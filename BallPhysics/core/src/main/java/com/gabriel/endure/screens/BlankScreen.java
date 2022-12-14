package com.gabriel.endure.screens;

import com.gabriel.endure.ProjectEndure;

import de.eskalon.commons.screen.ManagedScreen;

public class BlankScreen extends AbstractScreen {

    public BlankScreen(ProjectEndure context) {
        super(context);
    }

    @Override
    protected void create() {
        // do nothing
    }

    @Override
    public void hide() {
        // do nothing
    }

    @Override
    public void render(float delta) {
        // do nothing except having the screen cleared
    }

    @Override
    public void resize(int width, int height) {
        // do nothing
    }

    @Override
    public void dispose() {
        // do nothing
    }

}