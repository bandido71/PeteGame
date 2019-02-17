package com.mitchell.pete.screen

import com.badlogic.gdx.audio.Music
import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer
import com.badlogic.gdx.math.Intersector
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.utils.viewport.FitViewport
import com.mitchell.pete.PeteGame
import com.mitchell.pete.config.GameConfig
import com.mitchell.pete.entity.Acorn
import com.mitchell.pete.entity.Pete
import ktx.app.KtxScreen
import ktx.app.clearScreen
import ktx.collections.GdxArray
import ktx.graphics.use

class GameScreen(game: PeteGame) : KtxScreen {
    private var shapeRenderer = ShapeRenderer()
    private var camera = OrthographicCamera()
    private var viewport = FitViewport(GameConfig.WORLD_WIDTH, GameConfig.WORLD_HEIGHT, camera)
    private var batch = SpriteBatch()
    private val assetManager = game.assetManager

    private var tiledMap = assetManager.get<TiledMap>("pete.tmx")
    private var peteTexture = assetManager.get<Texture>("pete.png")
    private var acornTexture = assetManager.get<Texture>("acorn.png")
    private var jumpSound = assetManager.get<Sound>("jump.wav")
    private var acornSound = assetManager.get<Sound>("acorn.wav")
    private var gameMusic = assetManager.get<Music>("peteTheme.mp3")
    private var orthogonalTiledMapRenderer = OrthogonalTiledMapRenderer(tiledMap, batch)

    private var pete = Pete(peteTexture, jumpSound)
    private var acorns = GdxArray<Acorn>()

    init {
        // se debe centrar la cámara
        viewport.apply(true)
        orthogonalTiledMapRenderer.setView(camera)
        pete.setPosition(0f, GameConfig.WORLD_HEIGHT / 2)
        populateAcorns()
        gameMusic.isLooping = true
        gameMusic.play()
    }

    override fun render(delta: Float) {
        update(delta)
        clearScreen(Color.TEAL.r, Color.TEAL.g, Color.TEAL.b, Color.TEAL.a)
        draw()
        drawDebug()
    }

    override fun resize(width: Int, height: Int) {
        viewport.update(width, height, true)
    }

    override fun dispose() {
        batch.dispose()
        shapeRenderer.dispose()
    }

    private fun update(delta: Float) {
        pete.update(delta)
        stopPeteLeavingTheScreen()
        handlePeteCollision()
        handlePeteCollisionWithAcorn()
        updateCameraX()
    }

    private fun draw() {
        viewport.apply()
        batch.projectionMatrix = camera.combined
        orthogonalTiledMapRenderer.render()

        batch.use {
            acorns.forEach {
                it.draw(batch)
            }
            pete.draw(batch)
        }
    }

    private fun drawDebug() {
        viewport.apply()
        shapeRenderer.projectionMatrix = camera.combined

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line)
        pete.drawDebug(shapeRenderer)
        shapeRenderer.end()
    }

    private fun updateCameraX() {
        val tiledMapTileLayer = tiledMap.layers[0] as TiledMapTileLayer
        val levelWidth = tiledMapTileLayer.width * tiledMapTileLayer.tileWidth

        if ((pete.x > GameConfig.WORLD_WIDTH / 2) && (pete.x < (levelWidth - GameConfig.WORLD_WIDTH / 2))) {
            camera.position.set(pete.x, camera.position.y, camera.position.z)
            camera.update()
            orthogonalTiledMapRenderer.setView(camera)
        }
    }

    private fun stopPeteLeavingTheScreen() {
        if (pete.y < 0f) {
            pete.y = 0f
            pete.landed()
        }

        val tiledMapTileLayer = tiledMap.layers[0] as TiledMapTileLayer
        val levelWidth = tiledMapTileLayer.width * tiledMapTileLayer.tileWidth

        pete.x = MathUtils.clamp(pete.x, 0f, levelWidth - Pete.WIDTH)
    }

    private fun whichCellsDoesPeteCover(): GdxArray<CollisionCell> {
        val x = pete.x
        val y = pete.y
        val cellsCovered = GdxArray<CollisionCell>()
        val cellX = x / GameConfig.CELL_SIZE
        val cellY = y / GameConfig.CELL_SIZE

        // se obtiene celda del piso a la izquierda
        val bottomLeftCellX = MathUtils.floor(cellX)
        val bottomLeftCellY = MathUtils.floor(cellY)

        val tiledMapTileLayer = tiledMap.layers[0] as TiledMapTileLayer

        cellsCovered.add(CollisionCell(
                tiledMapTileLayer.getCell(bottomLeftCellX, bottomLeftCellY),
                bottomLeftCellX.toFloat(),
                bottomLeftCellY.toFloat()
        ))

        if (cellX % 1f != 0f && cellY % 1f != 0f) {
            // se obtiene celda de arriba a la derecha
            val topRightCellX = bottomLeftCellX + 1
            val topRightCellY = bottomLeftCellY + 1

            cellsCovered.add(CollisionCell(
                    tiledMapTileLayer.getCell(topRightCellX, topRightCellY),
                    topRightCellX.toFloat(),
                    topRightCellY.toFloat()
            ))
        }

        if (cellX % 1f != 0f) {
            // se obtiene celda del piso a la derecha
            val bottomRightCellX = bottomLeftCellX + 1
            val bottomRightCellY = bottomLeftCellY

            cellsCovered.add(CollisionCell(
                    tiledMapTileLayer.getCell(bottomRightCellX, bottomRightCellY),
                    bottomRightCellX.toFloat(),
                    bottomRightCellY.toFloat()
            ))
        }

        if (cellY % 1f != 0f) {
            // se obtiene celda del piso a la derecha
            val topLeftCellX = bottomLeftCellX
            val topLeftCellY = bottomLeftCellY + 1

            cellsCovered.add(CollisionCell(
                    tiledMapTileLayer.getCell(topLeftCellX, topLeftCellY),
                    topLeftCellX.toFloat(),
                    topLeftCellY.toFloat()
            ))
        }

        return cellsCovered
    }

    private fun filterOutNonTiledCells(cells: GdxArray<CollisionCell>): GdxArray<CollisionCell> {
        // usar un "mutable iterator" para remover elementos de un array, y luego utilizar forEach en el.
        val mutableIterator = cells.iterator()
        mutableIterator.forEach {
            if (it.isEmpty()) {
                mutableIterator.remove()
            }
        }

        return cells
    }

    private fun handlePeteCollision() {
        var peteCells = whichCellsDoesPeteCover()
        peteCells = filterOutNonTiledCells(peteCells)

        peteCells.forEach {
            val cellLevelX = it.cellX * GameConfig.CELL_SIZE
            val cellLevelY = it.cellY * GameConfig.CELL_SIZE

            val intersection = Rectangle()

            Intersector.intersectRectangles(
                    pete.collisionRectangle,
                    Rectangle(cellLevelX, cellLevelY, GameConfig.CELL_SIZE, GameConfig.CELL_SIZE),
                    intersection
            )

            if (intersection.height < intersection.width) {
                pete.setPosition(pete.x, intersection.y + intersection.height)
                pete.landed()
            } else if (intersection.width < intersection.height) {
                if (intersection.x == pete.x) pete.setPosition(intersection.x + intersection.width, pete.y)
                if (intersection.x > pete.x) pete.setPosition(intersection.x - Pete.WIDTH, pete.y)
            }
        }
    }

    private fun populateAcorns() {
        val mapLayer = tiledMap.layers["Collectables"]
        mapLayer.objects.forEach {
            acorns.add(Acorn(acornTexture,
                    it.properties.get("x", Float::class.java),
                    it.properties.get(
                            "y",
                            Float::class.java
                    )))
        }
    }

    private fun handlePeteCollisionWithAcorn() {
        // usar un "mutable iterator" para remover elementos de un array, y luego utilizar forEach en el.
        val mutableIterator = acorns.iterator()
        mutableIterator.forEach {
            if (pete.collisionRectangle.overlaps(it.collisionRectangle)) {
                acornSound.play()
                mutableIterator.remove()
            }
        }
    }

    inner class CollisionCell(private var cell: TiledMapTileLayer.Cell?, val cellX: Float, val cellY: Float) {
        fun isEmpty() = (cell == null)
    }
}