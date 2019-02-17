package com.mitchell.pete.screen

import com.badlogic.gdx.audio.Music
import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.utils.viewport.FitViewport
import com.mitchell.pete.PeteGame
import com.mitchell.pete.config.GameConfig
import ktx.app.KtxScreen
import ktx.app.clearScreen
import ktx.assets.load

class LoadingScreen(private val game: PeteGame) : KtxScreen {
    companion object {
        private const val PROGRESS_BAR_WIDTH = 100f
        private const val PROGRESS_BAR_HEIGHT = 25f
    }

    private var camera = OrthographicCamera()
    private var viewport = FitViewport(GameConfig.WORLD_WIDTH, GameConfig.WORLD_HEIGHT, camera)
    private var shapeRenderer = ShapeRenderer()

    private val assetManager = game.assetManager
    private var progress = 0f

    init {
        assetManager.load<TiledMap>("pete.tmx")
        assetManager.load<Texture>("pete.png")
        assetManager.load<Texture>("acorn.png")
        assetManager.load<Sound>("jump.wav")
        assetManager.load<Sound>("acorn.wav")
        assetManager.load<Music>("peteTheme.mp3")
    }

    override fun resize(width: Int, height: Int) {
        viewport.update(width, height, true)
    }

    override fun render(delta: Float) {
        update(delta)
        clearScreen(Color.BLACK.r, Color.BLACK.g, Color.BLACK.b, Color.BLACK.a)
        draw()
    }

    override fun dispose() {
        shapeRenderer.dispose()
    }

    private fun update(delta: Float) {
        if (assetManager.update()) {
            game.addScreen(GameScreen(game))
            game.setScreen<GameScreen>()
        } else {
            progress = assetManager.progress
        }
    }

    private fun draw() {
        viewport.apply()
        shapeRenderer.projectionMatrix = camera.combined

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)

        shapeRenderer.color = Color.WHITE
        shapeRenderer.rect((GameConfig.WORLD_WIDTH - PROGRESS_BAR_WIDTH) / 2f,
                (GameConfig.WORLD_HEIGHT - PROGRESS_BAR_HEIGHT) / 2f,
                progress * PROGRESS_BAR_WIDTH,
                PROGRESS_BAR_HEIGHT)

        shapeRenderer.end()
    }

}