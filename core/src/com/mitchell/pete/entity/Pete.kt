package com.mitchell.pete.entity

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle

class Pete(texture : Texture, private val jumpSound: Sound) {
    companion object {
        private const val MAX_X_SPEED = 2f
        private const val MAX_Y_SPEED = 2f
        const val WIDTH = 16f
        const val HEIGHT = 15f
        private const val MAX_JUMP_DISTANCE = 3 * HEIGHT
    }

    val collisionRectangle = Rectangle(0f, 0f, WIDTH, HEIGHT)

    private var blockJump = false
    private var jumpYDistance = 0f
    private var animationTimer = 0f
    private val walking : Animation<TextureRegion>
    private val standing : TextureRegion
    private val jumpUp : TextureRegion
    private val jumpDown : TextureRegion

    var x = 0f
        set(value) {
            field = value
            updateCollisionRectangle()
        }

    var y = 0f
        set(value) {
            field = value
            updateCollisionRectangle()
        }

    private var xSpeed = 0f
    private var ySpeed = 0f

    init {
        val regions = TextureRegion(texture).split(WIDTH.toInt(), HEIGHT.toInt())[0]
        walking = Animation(0.25f, regions[0], regions[1])
        walking.playMode = Animation.PlayMode.LOOP
        standing = regions[0]
        jumpUp = regions[2]
        jumpDown = regions[3]
    }

    fun update(delta : Float) {
        animationTimer += delta
        val input = Gdx.input

//        if (input.isKeyPressed(Input.Keys.RIGHT)) {
//            xSpeed = MAX_X_SPEED
//        } else if (input.isKeyPressed(Input.Keys.LEFT)) {
//            xSpeed = -MAX_X_SPEED
//        } else {
//            xSpeed = 0f
//        }

        xSpeed = when {
            input.isKeyPressed(Input.Keys.RIGHT) -> MAX_X_SPEED
            input.isKeyPressed(Input.Keys.LEFT) -> -MAX_X_SPEED
            else -> 0f
        }

//        when {
//            (input.isKeyPressed(Input.Keys.UP) && !blockJump) -> {
//                ySpeed = MAX_Y_SPEED
//                jumpYDistance += ySpeed
//                blockJump = jumpYDistance > MAX_JUMP_DISTANCE
//            }
//            else -> {
//                ySpeed = -MAX_Y_SPEED
//                blockJump = jumpYDistance > 0
//            }
//        }

        if (input.isKeyPressed(Input.Keys.UP) && !blockJump) {
            if (ySpeed != MAX_Y_SPEED) jumpSound.play()
            ySpeed = MAX_Y_SPEED
            jumpYDistance += ySpeed
            blockJump = jumpYDistance > MAX_JUMP_DISTANCE
        } else {
            ySpeed = -MAX_Y_SPEED
            blockJump = jumpYDistance > 0
        }

        x += xSpeed
        y += ySpeed
    }

    fun setPosition(x: Float, y: Float) {
        this.x = x
        this.y = y
    }

    fun drawDebug(shapeRenderer: ShapeRenderer) {
        shapeRenderer.rect(
                collisionRectangle.x,
                collisionRectangle.y,
                collisionRectangle.width,
                collisionRectangle.height
        )
    }

    fun draw(batch: SpriteBatch) {
        var toDraw = standing

        when {
            xSpeed != 0f -> toDraw = walking.getKeyFrame(animationTimer)
            ySpeed > 0f -> toDraw = jumpUp
            ySpeed < 0f -> toDraw = jumpDown
        }

        if (xSpeed < 0f) {
            if (!toDraw.isFlipX) toDraw.flip(true, false)
        } else if (xSpeed > 0f) {
            if (toDraw.isFlipX) toDraw.flip(true, false)
        }

        batch.draw(toDraw, x, y)
    }

    private fun updateCollisionRectangle() = collisionRectangle.setPosition(x, y)

    fun landed() {
        blockJump = false
        jumpYDistance = 0f
        ySpeed = 0f
    }

}