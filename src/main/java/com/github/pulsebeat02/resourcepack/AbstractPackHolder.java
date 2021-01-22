package com.github.pulsebeat02.resourcepack;

import java.io.IOException;

public interface AbstractPackHolder {

    void buildResourcePack() throws IOException;

    void onResourcepackBuild();

}
