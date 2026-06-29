# MotorLog

Aplicativo Android nativo para controle de manutenção de motos. Em vez de anotar trocas num caderno e depender da memória, o MotorLog registra cada troca de peça e **calcula** quando a próxima vence — a partir de um único dado vivo: a quilometragem atual da moto.

> Exemplo: registrada a troca de óleo aos 15.000 km (intervalo de 3.000 km), numa moto que hoje está com 16.000 km, o painel mostra *"Óleo do motor — faltam 2.000 km"*. Peças vencidas aparecem destacadas.

Projeto de aprendizado de desenvolvimento mobile (Kotlin/Android nativo), em evolução contínua.

## Status

MVP funcional, rodando em dispositivo real. A funcionalidade central — registrar uma troca e obter a recomendação calculada — está completa. Telas adicionais previstas no design (histórico de serviços, índice de cuidado, tema escuro) estão no roadmap. Ainda não há suíte de testes automatizados.

## Funcionalidades

- **Garagem** — cadastro e listagem de motos (modelo, placa, ano, quilometragem).
- **Atualizar km** — a ação central do app: rápida e com o valor atual pré-preenchido.
- **Registrar troca** — seleção da peça por busca (evitando erros de digitação) e informação do km da troca.
- **Painel da moto** — quilometragem em destaque e seção "Próximas trocas", com status por cor (em dia, próximo do vencimento, vencido, nunca trocada).
- **Catálogo de peças** — cerca de 50 itens com intervalos de manutenção realistas, editáveis pelo usuário.

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
| Persistência | Room 2.8.1 (processamento via KSP) |
| Build | Gradle (Kotlin DSL) com version catalog, AGP 9.2.1 |
| SDK | minSdk 28 · targetSdk 36 · compileSdk 36 |

A navegação é feita por estado (`when(telaAtual)`), sem biblioteca externa — uma escolha deliberada para reaproveitar estado entre telas neste estágio do projeto.

## Arquitetura

O código é organizado em camadas, mantendo a regra de negócio independente da interface e do framework:

```
com.development.motorlog
├── data/         Room: entidades (Moto, Peca, Registro), DAOs e AppDatabase
├── domain/       regra de negócio pura, sem Android (cálculo de recomendações)
├── ui/           telas Compose e ViewModels (state holders)
└── MainActivity  hospeda a navegação por estado
```

- **`data`** — `Moto`, `Peca` e `Registro` (esta última é a tabela de associação de um relacionamento N–N, com duas chaves estrangeiras). DAOs com `@Insert`, `@Update` e `@Query`.
- **`domain`** — `calcularRecomendacoes(...)`, `Recomendacao` e `StatusTroca`. Não conhece Compose nem Room: recebe dados e devolve recomendações.
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

O banco (`motorlog.db`) é criado no primeiro uso e populado com o catálogo de peças. No estágio atual as migrações são destrutivas — desinstalar o app zera os dados.

## Roadmap

- Gerenciamento de peças pela interface (criar/editar peça e intervalo) — em andamento.
- Registro de serviços e revisões (mão de obra), além das peças.
- Histórico de atividades por moto.
- Persistência do formulário na rotação (`rememberSaveable`) e mensagens de erro na própria interface.
- Tema escuro e índice de cuidado.
- Migrações não destrutivas e leitura reativa com `Flow`.

## Contribuindo

Contribuições são bem-vindas. Antes de um PR maior, abra uma issue descrevendo o bug ou a proposta, para alinhamento. Diretrizes:

- Preserve a separação de camadas — regra de negócio fica em `domain`, sem dependências de Android.
- Escreva mensagens de commit claras.
- Para mudanças de comportamento, descreva como testá-las.

Este é, antes de tudo, um projeto de estudo: o autor escreve cada linha como parte do aprendizado de Android. Por isso, issues bem descritas, revisões e sugestões de arquitetura são tão valiosas quanto código.

## Licença

A definir. Até a definição de uma licença, todos os direitos são reservados ao autor.
