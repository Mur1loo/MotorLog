package com.development.motorlog.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

// Uma foto do álbum da moto. O arquivo (JPEG já reduzido e girado) vive em filesDir/fotos/;
// aqui fica só o nome dele, o dia, o km da moto naquele dia e uma legenda opcional.
@Entity(
    foreignKeys = [ForeignKey(
        entity = Moto::class,
        parentColumns = ["id"],
        childColumns = ["motoId"],
        onDelete = ForeignKey.CASCADE,
    )],
    indices = [Index("motoId")],
)
data class FotoMoto(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val motoId: Long,
    val arquivo: String,
    val data: Long,
    val km: Int,
    val legenda: String = "",
)
