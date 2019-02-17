package com.mitchell.pete

import com.badlogic.gdx.Files
import com.badlogic.gdx.Screen
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver
import com.badlogic.gdx.maps.tiled.TmxMapLoader
import com.mitchell.pete.screen.LoadingScreen
import ktx.app.KtxGame
import ktx.assets.getResolver
import ktx.assets.setLoader

class PeteGame : KtxGame<Screen>() {

    val assetManager = AssetManager()

    override fun create() {

        val resolver = Files.FileType.Internal.getResolver()
        assetManager.setLoader(TmxMapLoader(resolver))

        val loadingScreen = LoadingScreen(this)

        addScreen(loadingScreen)
        setScreen<LoadingScreen>()
    }

    override fun dispose() {
        assetManager.dispose()
    }
}