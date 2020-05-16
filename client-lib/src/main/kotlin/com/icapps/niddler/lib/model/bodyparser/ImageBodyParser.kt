package com.icapps.niddler.lib.model.bodyparser

import com.chimerapps.discovery.utils.debug
import com.chimerapps.discovery.utils.logger
import com.icapps.niddler.lib.model.BodyFormat
import com.icapps.niddler.lib.model.BodyParser
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import javax.imageio.ImageIO

/**
 * @author Nicola Verbeeck
 *
 * Tries to parse the data as an image
 */
class ImageBodyParser : BodyParser<BufferedImage> {

    override fun parse(bodyFormat: BodyFormat, bytes: ByteArray): BufferedImage? {
        //TODO svg here or separately?

        return try {
            ImageIO.read(ByteArrayInputStream(bytes))
        } catch (e: Throwable) {
            logger<ImageBodyParser>().debug("Failed to parse image:", e)
            return null
        }
    }
}