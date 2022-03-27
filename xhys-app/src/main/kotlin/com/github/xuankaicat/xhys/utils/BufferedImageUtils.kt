package com.github.xuankaicat.xhys.utils

import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import javax.imageio.ImageIO

fun BufferedImage.toInputStream(): InputStream {
    val os = ByteArrayOutputStream()
    ImageIO.write(this, "png", os)
    return ByteArrayInputStream(os.toByteArray())
}