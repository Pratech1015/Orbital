package io.papermc.paper;

import com.google.common.base.Strings;
import io.papermc.paper.util.JarManifests;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.jar.Manifest;
import net.kyori.adventure.key.Key;
import net.minecraft.SharedConstants;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.Main;
import org.jetbrains.annotations.NotNull;

public record ServerBuildInfoImpl(
    Key brandId,
    String brandName,
    String minecraftVersionId,
    String minecraftVersionName,
    OptionalInt buildNumber,
    Instant buildTime,
    Optional<String> gitBranch,
    Optional<String> gitCommit
) implements ServerBuildInfo {
    private static final String ATTRIBUTE_BRAND_ID = "Brand-Id";
    private static final String ATTRIBUTE_BRAND_NAME = "Brand-Name";
    private static final String ATTRIBUTE_BUILD_TIME = "Build-Time";
    private static final String ATTRIBUTE_BUILD_NUMBER = "Build-Number";
    private static final String ATTRIBUTE_GIT_BRANCH = "Git-Branch";
    private static final String ATTRIBUTE_GIT_COMMIT = "Git-Commit";

    private static final String BRAND_PAPER_NAME = "Orbital";

    private static final String BUILD_DEV = "Pratech";

    public ServerBuildInfoImpl() {
        this(JarManifests.manifest(CraftServer.class));
    }

    private ServerBuildInfoImpl(final Manifest manifest) {
        this(
            getManifestAttribute(manifest, ATTRIBUTE_BRAND_ID)
                .map(Key::key)
                .orElse(Key.key("orbital", "pratech")),
            getManifestAttribute(manifest, ATTRIBUTE_BRAND_NAME)
                .orElse(BRAND_PAPER_NAME),
            SharedConstants.getCurrentVersion().id(),
            SharedConstants.getCurrentVersion().name(),
            getManifestAttribute(manifest, ATTRIBUTE_BUILD_NUMBER)
                .map(Integer::parseInt)
                .map(OptionalInt::of)
                .orElse(OptionalInt.empty()),
            getManifestAttribute(manifest, ATTRIBUTE_BUILD_TIME)
                .map(Instant::parse)
                .orElse(Main.BOOT_TIME),
            getManifestAttribute(manifest, ATTRIBUTE_GIT_BRANCH),
            getManifestAttribute(manifest, ATTRIBUTE_GIT_COMMIT)
        );
    }

    @Override
    public boolean isBrandCompatible(final @NotNull Key brandId) {
        return brandId.equals(this.brandId);
    }

    @Override
    public @NotNull String asString(final @NotNull StringRepresentation representation) {
        final StringBuilder sb = new StringBuilder();

        sb.append(this.minecraftVersionId);
        final String apiVersion = getManifestAttribute(
            JarManifests.manifest(CraftServer.class),
            "Api-Version"
        ).orElse("R0.1-SNAPSHOT");

        sb.append("-");
        sb.append(apiVersion);

        this.gitCommit.ifPresent(commit -> {
            sb.append(" (Git: ");
            sb.append(commit.substring(0, Math.min(7, commit.length())));
            sb.append(")");
        });

        if (representation == StringRepresentation.VERSION_FULL && this.buildTime != null) {
            sb.append(" | Built by Pratech on ");
            sb.append(this.buildTime.truncatedTo(ChronoUnit.SECONDS));
        }

        return sb.toString();
    }

    private static Optional<String> getManifestAttribute(final Manifest manifest, final String name) {
        final String value = manifest != null ? manifest.getMainAttributes().getValue(name) : null;
        return Optional.ofNullable(Strings.emptyToNull(value));
    }
}
