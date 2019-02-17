package com.mitchell.pete.entity

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Rectangle

class Acorn(private val texture: Texture, x: Float, y: Float) {
    companion object {
        const val WIDTH = 16f
        const val HEIGHT = 16f
    }

    val collisionRectangle: Rectangle

    private var x = 0f
    private var y = 0f

    init {
        this.x = x
        this.y = y
        this.collisionRectangle = Rectangle(x, y, WIDTH, HEIGHT)
    }

    fun draw(batch: SpriteBatch){
        batch.draw(texture, x, y)
    }

}