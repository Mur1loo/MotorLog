# MotorLog

Aplicativo Android nativo para controle de manutenção de motos. Em vez de anotar trocas num caderno e depender da memória, o MotorLog registra cada troca de peça e **calcula** quando a próxima vence — a partir de um único dado vivo: a quilometragem atual da moto.

> Exemplo: registrada a troca de óleo aos 15.000 km (intervalo de 3.000 km), numa moto que hoje está com 16.000 km, o painel mostra *"Óleo do motor — faltam 2.000 km"*. Peças vencidas aparecem destacadas.

Projeto de aprendizado de desenvolvimento mobile (Kotlin/Android nativo), em evolução contínua.

## Status

MVP funcional, rodando em dispositivo real. A funcionalidade central — registrar uma troca e obter a recomendação calculada — está completa, assim como serviços de manutenção (registro, histórico, detalhe com decomposição de custo), a tela "Trocas por km" agrupada por urgência e o tema escuro do protótipo. O banco (Room, schema v11) evolui por migrations explícitas e testadas; a regra de negócio tem suíte unitária.

## Funcionalidades

- **Garagem** — cadastro e listagem de motos (modelo, placa, ano, quilometragem).
- **Atualizar km** — a ação central do app: rápida e com o valor atual pré-preenchido.
- **Troquei uma peça** — a troca feita por conta própria (em casa, com um amigo): peça por busca, km, **valor pago na peça** (opcional) e dia. Entra no gasto total e no gasto do mês; dá pra corrigir ou excluir no Histórico.
- **Painel da moto** — quilometragem em destaque, gasto total, serviços, "Próximas trocas" com status por cor (em dia, próximo do vencimento, vencido) e atividade recente.
- **Trocas por km** — todas as peças agrupadas por urgência (vencidas / perto de vencer / mais adiante / sem registro), cada uma com barra de progresso do intervalo.
- **Serviços** — registro de visita à oficina (tipo, custo, oficina, data, km) com peças trocadas e preço; histórico e detalhe (peças + mão de obra = total). As peças trocadas num serviço alimentam as recomendações.
- **Lembretes 2x por dia (7h e 19h)** — notificação quando há troca vencida/perto de vencer (WorkManager); o km parado há 3+ dias só é cobrado no lembrete das 19h, e o mesmo aviso não se repete no mesmo dia. Tocar abre o painel da moto.
- **Ritmo de uso** — km/mês estimado pelo histórico de atualizações, convertendo "faltam 400 km" em "~6 dias".
- **Alertas na Garagem** — cada moto mostra quantas trocas estão vencidas/perto, sem precisar abrir o painel.
- **Histórico em PDF** — relatório A4 da moto (foto de capa, resumo de gastos, situação das peças e todo o histórico de oficina e trocas por conta própria) pra mandar no WhatsApp: pro comprador, pro mecânico ou pra guardar.
- **Álbum da moto** (aba Fotos) — fotos pela câmera ou galeria (sem pedir permissão: câmera do sistema e seletor de fotos do Android), com legenda, dia e km. A capa aparece na Garagem, no Painel e no PDF. Ficam só no armazenamento privado do app.
- **Cor da moto** — cada moto tem a sua cor no app (8 opções), usada no painel, no odômetro e no PDF.
- **Backup** — CSV com motos, trocas, serviços e catálogo, via compartilhar, restaurável em outro celular (as fotos não entram no CSV). O backup automático do Google também fica sem as fotos, pra não passar do teto de 25 MB, acima do qual nada seria salvo; a transferência direta entre celulares leva tudo.
- **Apoio solidário (Pix)** — sempre disponível no "Sobre". O pedido automático ao abrir o app só aparece para quem já usa o app há 2+ semanas, atualizou o km em 6+ dias e segue ativo, no máximo 1x por mês (`domain/Apoio.kt`).
- **Catálogo de peças** — cerca de 50 itens com intervalos de manutenção realistas, editáveis pelo usuário, com busca sem acento.
- **Exclusão com confirmação** de moto, peça e serviço, com cascata via foreign keys.

## Como a recomendação é calculada

A regra de negócio é uma função pura, sem dependência de Android e isolada na camada `domain`:

```
para cada peça:
    última troca  = registro de maior km daquela peça
    próxima troca = última troca + intervalo da peça
    km restante   = próxima troca − km atual da moto
    status        = EM_DIA | PERTO | VENCIDA | NUNCA_TROCADA
```

Como todo o cálculo parte do km atual, "atualizar km" é a ação mais importante da interface, e o recálculo é sempre relativo ao estado real da moto.

## Stack

| Área | Tecnologia |
| --- | --- |
| Linguagem | Kotlin 2.2.10 |
| Interface | Jetpack Compose (Material 3) |
| Persistência | Room 2.8.1 (processamento via KSP), schema v11 com migrations explícitas e testadas |
| Tarefas em segundo plano | WorkManager (lembretes 2x por dia) |
| Testes | JUnit 4 (domínio) · `room-testing`/`MigrationTestHelper` (instrumentado) |
| Build | Gradle (Kotlin DSL) com version catalog, AGP 9.2.1 |
| SDK | minSdk 28 · targetSdk 36 · compileSdk 36 |

A navegação é feita por estado (`when(telaAtual)`), sem biblioteca externa — uma escolha deliberada para reaproveitar estado entre telas neste estágio do projeto. As telas ficam numa pilha (`ui/navegacao/Pilha.kt`, Kotlin puro e testado): o voltar leva para a tela de onde o usuário veio, e trocar de aba da moto não empilha.

## Arquitetura

O código é organizado em camadas, mantendo a regra de negócio independente da interface e do framework:

```
com.development.motorlog
├── data/         Room: entidades (Moto, Peca, Registro, Servico, HistoricoKm, FotoMoto), DAOs, migrations e AppDatabase
├── fotos/        ArmazemDeFotos (salva reduzida/girada em filesDir/fotos, miniaturas com cache)
├── relatorio/    HistoricoPdf (desenha o PDF com o PdfDocument do Android e abre o compartilhar)
├── lembrete/     LembreteWorker (notificação diária via WorkManager)
├── domain/       regra de negócio pura, sem Android (cálculo de recomendações)
├── ui/           telas Compose e ViewModels (state holders)
└── MainActivity  hospeda a navegação por estado
```

- **`data`** — `Moto`, `Peca`, `Registro` (associação N–N moto×peça, com FK opcional para o serviço em que a troca aconteceu, preço e dia), `Servico` (visita à oficina) e `FotoMoto` (álbum; o arquivo fica em `filesDir/fotos`). DAOs com `@Insert`, `@Update`, `@Delete` e `@Query`. Toda mudança de schema é uma `Migration` explícita com o JSON exportado em `app/schemas/` e um teste em `MigrationTest`.
- **`domain`** — `calcularRecomendacoes(...)`, `Recomendacao` e `StatusTroca`; também custos, alertas, ritmo, exportação/importação, a escolha da capa e o conteúdo do PDF (`montarRelatorio`). Não conhece Compose nem Room: recebe dados e devolve resultados.
- **`ui`** — uma tela por arquivo (`GaragemScreen`, `PainelScreen`, `RegistroScreen`, etc.) e ViewModels que mantêm o estado e acessam os DAOs por meio de `viewModelScope`.

## Executando localmente

Pré-requisitos: Android Studio recente (compatível com AGP 9), JDK 17 ou superior, e um dispositivo ou emulador com Android 9 (API 28) ou acima.

```bash
git clone https://github.com/Mur1loo/MotorLog.git
cd MotorLog
```

1. Abra o projeto no Android Studio e aguarde o Gradle sync concluir.
2. Conecte um dispositivo com depuração USB habilitada, ou inicie um emulador.
3. Execute a configuração `app`.

O banco (`motorlog.db`) é criado no primeiro uso e populado com o catálogo de peças. Atualizar o app por cima preserva os dados: as migrations (v5→v11) são explícitas e não destrutivas.

### Testes

```bash
./gradlew testDebugUnitTest                 # regra de negócio (calcularRecomendacoes)
./gradlew connectedDebugAndroidTest         # migrations 5→…→11 (precisa de emulador/dispositivo)
./gradlew assembleDebug testDebugUnitTest lint
```

Convenções: datas são `Long` em millis à meia-noite UTC e sempre formatadas em UTC; `domain/` não importa `android.*`/`androidx.*`; schema novo = bump de versão + `Migration` + `schemas/N.json` + teste.

## Princípios de UX

O público-alvo é quem usa a moto para trabalhar, não quem gosta de tecnologia. Por isso: linguagem do dia a dia ("Troquei uma peça", "Fui à oficina"), números como no painel ("16.100 km"), uma ação principal grande por tela, confirmação visível depois de salvar, estados vazios que dizem o próximo passo e alvos de toque generosos.

## Roadmap

- Widget na tela inicial para atualizar o km sem abrir o app.
- Backup completo em arquivo único (dados + fotos do álbum).
- Marcos da moto ("passou dos 50.000 km!") e foto do painel junto da atualização de km.
- Leitura reativa com `Flow`; índice de cuidado.

## Contribuindo

Contribuições são bem-vindas. Antes de um PR maior, abra uma issue descrevendo o bug ou a proposta, para alinhamento. Diretrizes:

- Preserve a separação de camadas — regra de negócio fica em `domain`, sem dependências de Android.
- Escreva mensagens de commit claras.
- Para mudanças de comportamento, descreva como testá-las.

Este é, antes de tudo, um projeto de estudo: o autor escreve cada linha como parte do aprendizado de Android. Por isso, issues bem descritas, revisões e sugestões de arquitetura são tão valiosas quanto código.

## Licença

A definir. Até a definição de uma licença, todos os direitos são reservados ao autor.
