package com.development.motorlog.ui.screens

import android.content.ActivityNotFoundException
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.development.motorlog.R
import com.development.motorlog.data.FotoMoto
import com.development.motorlog.data.Moto
import com.development.motorlog.domain.AntesEDepois
import com.development.motorlog.domain.descreverIntervalo
import com.development.motorlog.domain.escolherCapa
import com.development.motorlog.domain.montarAntesEDepois
import com.development.motorlog.domain.nomeDaMoto
import com.development.motorlog.domain.tituloDoAntesEDepois
import com.development.motorlog.fotos.ArmazemDeFotos
import com.development.motorlog.relatorio.compartilharCartao
import com.development.motorlog.relatorio.gerarAntesEDepois
import com.development.motorlog.ui.components.BikeBadge
import com.development.motorlog.ui.components.BotaoPrimario
import com.development.motorlog.ui.components.BotaoSecundario
import com.development.motorlog.ui.components.ConfirmarExclusaoDialog
import com.development.motorlog.ui.components.FotoArquivo
import com.development.motorlog.ui.components.MlCard
import com.development.motorlog.ui.components.MlTextField
import com.development.motorlog.ui.components.Pill
import com.development.motorlog.ui.components.SectionLabel
import com.development.motorlog.ui.theme.MlFormas
import com.development.motorlog.ui.theme.accentDaMoto
import com.development.motorlog.ui.util.formatarData
import com.development.motorlog.ui.util.formatarKm
import com.development.motorlog.ui.viewModels.FotoViewModel
import kotlinx.coroutines.launch

// sugestões de legenda: o álbum conta a história da moto, não é só "foto 1, foto 2"
private val LEGENDAS_SUGERIDAS = listOf("O dia em que ela chegou", "Depois da lavagem", "Viagem", "Antes e depois", "Peça nova", "Rolê com a galera")

// Álbum da moto: capa em destaque, grade com as fotos (legenda, dia e km de cada uma) e as duas
// formas de adicionar — câmera ou galeria. As fotos ficam só no celular (armazenamento do app).
@Composable
fun FotosScreen(
    moto: Moto,
    onDefinirCapa: (FotoMoto) -> Unit,
    onMensagem: (String) -> Unit,
    modifier: Modifier = Modifier,
    fotoViewModel: FotoViewModel = viewModel(),
) {
    val contexto = LocalContext.current
    val accent = accentDaMoto(moto)
    val fotos = fotoViewModel.fotos
    val capa = escolherCapa(fotos, moto.fotoCapaId)
    var abertaId by rememberSaveable { mutableStateOf<Long?>(null) }
    val aberta = fotos.find { it.id == abertaId }
    // antes e depois: modo de escolher 2 fotos (a mais antiga vira o "antes") e a prévia pra compartilhar
    var escolhendoPar by rememberSaveable { mutableStateOf(false) }
    var primeiraId by rememberSaveable { mutableStateOf<Long?>(null) }
    var segundaId by rememberSaveable { mutableStateOf<Long?>(null) }
    var vendoPar by rememberSaveable { mutableStateOf(false) }
    val primeira = fotos.find { it.id == primeiraId }
    val segunda = fotos.find { it.id == segundaId }
    val par = if (primeira != null && segunda != null) montarAntesEDepois(primeira, segunda) else null
    val escopo = rememberCoroutineScope()
    fun alternarEscolha(id: Long) {
        when (id) {
            primeiraId -> { primeiraId = segundaId; segundaId = null }
            segundaId -> segundaId = null
            else -> if (primeiraId == null) primeiraId = id else segundaId = id
        }
    }
    fun sairDaEscolha() { escolhendoPar = false; primeiraId = null; segundaId = null; vendoPar = false }

    LaunchedEffect(moto.id) { fotoViewModel.carregar(moto.id) }

    val aoFalhar = { onMensagem("Não consegui abrir essa imagem. Tente outra.") }
    val galeria = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) fotoViewModel.receberImagem(uri, aoFalhar)
    }
    // a Uri de destino da câmera precisa sobreviver à ida pro app da câmera (e a um giro de tela)
    var uriDaCamera by rememberSaveable { mutableStateOf<String?>(null) }
    val camera = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { ok ->
        val uri = uriDaCamera
        uriDaCamera = null
        if (ok && uri != null) fotoViewModel.receberImagem(Uri.parse(uri), aoFalhar)
        else ArmazemDeFotos.limparTemporariosDaCamera(contexto)
    }
    val tirarFoto = {
        val uri = ArmazemDeFotos.novaUriDaCamera(contexto)
        uriDaCamera = uri.toString()
        try {
            camera.launch(uri)
        } catch (_: ActivityNotFoundException) {
            onMensagem("Não achei um app de câmera. Escolha uma foto da galeria.")
        }
    }
    val escolherDaGaleria = { galeria.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = modifier.fillMaxSize().padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            if (capa == null) {
                // ── álbum vazio: o convite ──
                MlCard(pad = 20.dp) {
                    BikeBadge(accent, tamanho = 64.dp)
                    Spacer(Modifier.height(14.dp))
                    Text(if (moto.apelido.isNotBlank()) "A história da ${nomeDaMoto(moto)}" else "A história da sua ${moto.modelo}", style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "O dia em que ela chegou, a primeira viagem, o brilho depois da lavagem, o antes e depois de um capricho. " +
                            "Guarde aqui — cada foto leva o dia e o km em que foi tirada.",
                        style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "As fotos ficam só no seu celular. A primeira vira a capa da moto na Garagem.",
                        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                // ── capa em destaque ──
                Box(Modifier.fillMaxWidth().aspectRatio(16f / 10f).clip(MlFormas.card).clickable(onClickLabel = "ver a foto") { abertaId = capa.id }) {
                    FotoArquivo(capa.arquivo, Modifier.fillMaxSize(), ladoMaxPx = 1280, descricao = "Capa da moto")
                    Box(
                        Modifier.fillMaxSize().background(
                            Brush.verticalGradient(0.55f to Color.Transparent, 1f to Color.Black.copy(alpha = 0.75f)),
                        ),
                    )
                    Column(Modifier.align(Alignment.BottomStart).padding(14.dp)) {
                        Pill("Capa", accent)
                        Spacer(Modifier.height(6.dp))
                        Text(capa.legenda.ifBlank { nomeDaMoto(moto) }, style = MaterialTheme.typography.titleLarge, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text("${formatarData(capa.data)} · ${formatarKm(capa.km)}", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.8f))
                    }
                }
            }
        }
        item(span = { GridItemSpan(maxLineSpan) }) {
            Column {
                BotaoPrimario("Tirar foto agora", tirarFoto, icone = R.drawable.ic_ml_plus, altura = 52.dp, enabled = !fotoViewModel.processando)
                Spacer(Modifier.height(8.dp))
                BotaoSecundario("Escolher da galeria", escolherDaGaleria, Modifier.fillMaxWidth(), icone = R.drawable.ic_ml_doc, enabled = !fotoViewModel.processando)
                if (fotoViewModel.processando) {
                    Row(Modifier.padding(top = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                        Text("  Preparando a foto…", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                if (fotos.size >= 2 && !escolhendoPar) {
                    Spacer(Modifier.height(8.dp))
                    BotaoSecundario("Antes e depois", { escolhendoPar = true }, Modifier.fillMaxWidth(), icone = R.drawable.ic_ml_share)
                }
            }
        }
        if (escolhendoPar) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                MlCard(pad = 14.dp, cor = accent.copy(alpha = 0.12f), borda = accent) {
                    Text("Antes e depois", style = MaterialTheme.typography.titleSmall)
                    Text(
                        when {
                            primeiraId == null -> "Toque nas duas fotos: a de antes e a de depois. A mais antiga fica em cima."
                            segundaId == null -> "Agora a outra foto."
                            else -> "Pronto! Veja como ficou."
                        },
                        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        BotaoSecundario("Ver como ficou", { vendoPar = true }, Modifier.weight(1f), icone = R.drawable.ic_ml_check, enabled = par != null)
                        TextButton(onClick = ::sairDaEscolha) { Text("Cancelar") }
                    }
                }
            }
        }
        if (fotos.isNotEmpty()) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                SectionLabel("${fotos.size} foto${if (fotos.size == 1) "" else "s"}", Modifier.padding(top = 4.dp))
            }
        }
        items(fotos, key = { it.id }) { foto ->
            val escolhida = escolhendoPar && (foto.id == primeiraId || foto.id == segundaId)
            Column(
                Modifier.clickable(onClickLabel = if (escolhendoPar) "escolher pro antes e depois" else "ver a foto") {
                    if (escolhendoPar) alternarEscolha(foto.id) else abertaId = foto.id
                },
            ) {
                Box {
                    FotoArquivo(
                        foto.arquivo,
                        Modifier.fillMaxWidth().aspectRatio(1f).clip(MlFormas.campo)
                            .then(if (escolhida) Modifier.border(3.dp, accent, MlFormas.campo) else Modifier),
                        descricao = foto.legenda.ifBlank { null },
                    )
                    if (escolhida) Pill("Escolhida", accent, Modifier.align(Alignment.TopStart).padding(8.dp), fundo = Color.Black.copy(alpha = 0.6f))
                }
                Spacer(Modifier.height(5.dp))
                Text(foto.legenda.ifBlank { formatarData(foto.data) }, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    if (foto.legenda.isBlank()) formatarKm(foto.km) else "${formatarData(foto.data)} · ${formatarKm(foto.km)}",
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1,
                )
            }
        }
        item(span = { GridItemSpan(maxLineSpan) }) { Spacer(Modifier.height(16.dp)) }
    }

    fotoViewModel.pendente?.let { arquivo ->
        NovaFotoDialog(
            arquivo = arquivo,
            primeira = fotos.isEmpty(),
            onGuardar = { legenda -> fotoViewModel.confirmarPendente(moto, legenda) { onMensagem("Foto guardada no álbum.") } },
            onDescartar = { fotoViewModel.descartarPendente() },
        )
    }

    if (vendoPar && par != null) {
        var gerando by remember { mutableStateOf(false) }
        AntesEDepoisDialog(
            par = par,
            gerando = gerando,
            onCompartilhar = { titulo ->
                gerando = true
                escopo.launch {
                    val arquivo = runCatching { gerarAntesEDepois(contexto, moto, par, titulo) }.getOrNull()
                    gerando = false
                    if (arquivo != null) {
                        compartilharCartao(contexto, arquivo, "$titulo · ${nomeDaMoto(moto)} 🏍️")
                        sairDaEscolha()
                    } else {
                        onMensagem("Não consegui montar a imagem. Tente de novo.")
                    }
                }
            },
            onFechar = { vendoPar = false },
        )
    }

    if (aberta != null) {
        VisualizadorDeFoto(
            foto = aberta,
            ehCapa = aberta.id == capa?.id,
            onFechar = { abertaId = null },
            onDefinirCapa = { onDefinirCapa(aberta); onMensagem("Essa agora é a capa da moto.") },
            onSalvarLegenda = { fotoViewModel.atualizarLegenda(aberta, it) },
            onExcluir = { fotoViewModel.excluir(aberta) { abertaId = null } },
        )
    }
}

// Depois de tirar/escolher: prévia + legenda opcional (com sugestões de 1 toque)
@Composable
private fun NovaFotoDialog(arquivo: String, primeira: Boolean, onGuardar: (String) -> Unit, onDescartar: () -> Unit) {
    var legenda by rememberSaveable { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = {},   // não fecha tocando fora: a foto se perderia sem querer
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        title = { Text(if (primeira) "Primeira foto do álbum!" else "Nova foto", style = MaterialTheme.typography.titleLarge) },
        text = {
            Column {
                FotoArquivo(arquivo, Modifier.fillMaxWidth().aspectRatio(4f / 3f).clip(MlFormas.campo), ladoMaxPx = 800)
                Spacer(Modifier.height(12.dp))
                MlTextField(legenda, { legenda = it.take(60) }, "Legenda (opcional)")
                Spacer(Modifier.height(4.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy((-6).dp)) {
                    LEGENDAS_SUGERIDAS.forEach { s ->
                        FilterChip(selected = legenda == s, onClick = { legenda = s }, label = { Text(s) }, shape = MlFormas.pill)
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = { onGuardar(legenda) }) { Text("Guardar no álbum") } },
        dismissButton = { TextButton(onClick = onDescartar) { Text("Descartar") } },
    )
}

// sugestões de título do antes e depois (a legenda da foto de depois já vem preenchida)
private val TITULOS_ANTES_DEPOIS = listOf("Depois da lavagem", "Peça nova", "Personalização", "Restauração")

// Prévia do antes e depois: as duas fotos empilhadas, o tempo entre elas e o título da imagem
@Composable
private fun AntesEDepoisDialog(par: AntesEDepois, gerando: Boolean, onCompartilhar: (String) -> Unit, onFechar: () -> Unit) {
    var titulo by rememberSaveable(par.antes.id, par.depois.id) { mutableStateOf(tituloDoAntesEDepois(par)) }
    AlertDialog(
        onDismissRequest = onFechar,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        title = { Text("Antes e depois", style = MaterialTheme.typography.titleLarge) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                listOf("Antes" to par.antes, "Depois" to par.depois).forEach { (rotulo, foto) ->
                    Box(Modifier.fillMaxWidth().aspectRatio(16f / 9f).clip(MlFormas.campo)) {
                        FotoArquivo(foto.arquivo, Modifier.fillMaxSize(), ladoMaxPx = 800, descricao = rotulo)
                        Pill(rotulo, Color.White, Modifier.align(Alignment.TopStart).padding(8.dp), fundo = Color.Black.copy(alpha = 0.55f))
                        Text(
                            "${formatarData(foto.data)} · ${formatarKm(foto.km)}", style = MaterialTheme.typography.labelSmall, color = Color.White,
                            modifier = Modifier.align(Alignment.BottomStart).background(Color.Black.copy(alpha = 0.45f)).padding(horizontal = 8.dp, vertical = 4.dp),
                        )
                    }
                    Spacer(Modifier.height(6.dp))
                }
                Text(descreverIntervalo(par).replaceFirstChar { it.uppercase() }, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(10.dp))
                MlTextField(titulo, { titulo = it.take(40) }, "Título da imagem")
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy((-6).dp)) {
                    TITULOS_ANTES_DEPOIS.forEach { s ->
                        FilterChip(selected = titulo == s, onClick = { titulo = s }, label = { Text(s) }, shape = MlFormas.pill)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onCompartilhar(titulo.trim()) }, enabled = !gerando) { Text(if (gerando) "Montando…" else "Compartilhar") }
        },
        dismissButton = { TextButton(onClick = onFechar) { Text("Voltar") } },
    )
}

// Foto em tela cheia, com legenda, dia/km e as ações (capa, legenda, excluir)
@Composable
private fun VisualizadorDeFoto(
    foto: FotoMoto,
    ehCapa: Boolean,
    onFechar: () -> Unit,
    onDefinirCapa: () -> Unit,
    onSalvarLegenda: (String) -> Unit,
    onExcluir: () -> Unit,
) {
    var editandoLegenda by rememberSaveable { mutableStateOf(false) }
    var confirmarExclusao by rememberSaveable { mutableStateOf(false) }
    Dialog(onDismissRequest = onFechar, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Box(Modifier.fillMaxSize().background(Color.Black)) {
            FotoArquivo(foto.arquivo, Modifier.fillMaxSize(), ladoMaxPx = 2048, contentScale = ContentScale.Fit, descricao = foto.legenda.ifBlank { "Foto da moto" })
            IconButton(onClick = onFechar, modifier = Modifier.statusBarsPadding().padding(8.dp).align(Alignment.TopEnd)) {
                Icon(painterResource(R.drawable.ic_ml_close), contentDescription = "Fechar", tint = Color.White)
            }
            Column(
                Modifier.align(Alignment.BottomCenter).fillMaxWidth()
                    .background(Brush.verticalGradient(0f to Color.Transparent, 0.35f to Color.Black.copy(alpha = 0.85f)))
                    .navigationBarsPadding()
                    .padding(start = 16.dp, end = 16.dp, top = 36.dp, bottom = 12.dp),
            ) {
                if (foto.legenda.isNotBlank()) Text(foto.legenda, style = MaterialTheme.typography.titleLarge, color = Color.White)
                Text("${formatarData(foto.data)} · ${formatarKm(foto.km)} no painel", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.8f))
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                    if (ehCapa) Text("É a capa", style = MaterialTheme.typography.labelMedium, color = Color.White.copy(alpha = 0.7f), modifier = Modifier.padding(horizontal = 12.dp))
                    else TextButton(onClick = onDefinirCapa) { Text("Usar como capa") }
                    TextButton(onClick = { editandoLegenda = true }) { Text("Legenda") }
                    Spacer(Modifier.weight(1f))
                    TextButton(onClick = { confirmarExclusao = true }) { Text("Excluir", color = MaterialTheme.colorScheme.error) }
                }
            }
        }
    }

    if (editandoLegenda) {
        var texto by rememberSaveable { mutableStateOf(foto.legenda) }
        AlertDialog(
            onDismissRequest = { editandoLegenda = false },
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            title = { Text("Legenda", style = MaterialTheme.typography.titleLarge) },
            text = { MlTextField(texto, { texto = it.take(60) }, "Ex.: Viagem pra praia") },
            confirmButton = { TextButton(onClick = { onSalvarLegenda(texto); editandoLegenda = false }) { Text("Salvar") } },
            dismissButton = { TextButton(onClick = { editandoLegenda = false }) { Text("Cancelar") } },
        )
    }
    if (confirmarExclusao) {
        ConfirmarExclusaoDialog(
            texto = "Excluir esta foto do álbum? Ela só existe aqui no app.",
            onConfirmar = { confirmarExclusao = false; onExcluir() },
            onCancelar = { confirmarExclusao = false },
        )
    }
}
