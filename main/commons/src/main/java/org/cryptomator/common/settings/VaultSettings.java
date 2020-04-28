/*******************************************************************************
 * Copyright (c) 2017 Skymatic UG (haftungsbeschränkt).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the accompanying LICENSE file.
 *******************************************************************************/
package org.cryptomator.common.settings;

import com.google.common.base.Strings;
import com.google.common.io.BaseEncoding;
import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.apache.commons.lang3.StringUtils;
import org.fxmisc.easybind.EasyBind;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

/**
 * The settings specific to a single vault.
 * TODO: Change the name of individualMountPath and its derivatives to customMountPath
 */
public class VaultSettings {

	public static final boolean DEFAULT_UNLOCK_AFTER_STARTUP = false;
	public static final boolean DEFAULT_REAVEAL_AFTER_MOUNT = true;
	public static final boolean DEFAULT_USES_INDIVIDUAL_MOUNTPATH = false;
	public static final boolean DEFAULT_USES_READONLY_MODE = false;
	public static final String DEFAULT_MOUNT_FLAGS = "";
	public static final int DEFAULT_PATH_LENGTH_LIMITATION = -1;
	
	private static final Random RNG = new Random(); 

	private final String id;
	private final ObjectProperty<Path> path = new SimpleObjectProperty();
	private final StringProperty mountName = new SimpleStringProperty();
	private final StringProperty winDriveLetter = new SimpleStringProperty();
	private final BooleanProperty unlockAfterStartup = new SimpleBooleanProperty(DEFAULT_UNLOCK_AFTER_STARTUP);
	private final BooleanProperty revealAfterMount = new SimpleBooleanProperty(DEFAULT_REAVEAL_AFTER_MOUNT);
	private final BooleanProperty usesIndividualMountPath = new SimpleBooleanProperty(DEFAULT_USES_INDIVIDUAL_MOUNTPATH);
	private final StringProperty individualMountPath = new SimpleStringProperty();
	private final BooleanProperty usesReadOnlyMode = new SimpleBooleanProperty(DEFAULT_USES_READONLY_MODE);
	private final StringProperty mountFlags = new SimpleStringProperty(DEFAULT_MOUNT_FLAGS);
	private final IntegerProperty pathLengthLimitation = new SimpleIntegerProperty(DEFAULT_PATH_LENGTH_LIMITATION);

	public VaultSettings(String id) {
		this.id = Objects.requireNonNull(id);

		EasyBind.subscribe(path, this::deriveMountNameFromPath);
	}

	Observable[] observables() {
		return new Observable[]{path, mountName, winDriveLetter, unlockAfterStartup, revealAfterMount, usesIndividualMountPath, individualMountPath, usesReadOnlyMode, mountFlags, pathLengthLimitation};
	}

	private void deriveMountNameFromPath(Path path) {
		if (path != null && StringUtils.isBlank(mountName.get())) {
			mountName.set(normalizeMountName(path.getFileName().toString()));
		}
	}

	public static VaultSettings withRandomId() {
		return new VaultSettings(generateId());
	}

	private static String generateId() {
		byte[] randomBytes = new byte[9];
		RNG.nextBytes(randomBytes);
		return BaseEncoding.base64Url().encode(randomBytes);
	}

	public static String normalizeMountName(String mountName) {
		String normalizedMountName = StringUtils.stripAccents(mountName);
		StringBuilder builder = new StringBuilder();
		for (char c : normalizedMountName.toCharArray()) {
			if (Character.isWhitespace(c)) {
				if (builder.length() == 0 || builder.charAt(builder.length() - 1) != '_') {
					builder.append('_');
				}
			} else if (c < 127 && Character.isLetterOrDigit(c)) {
				builder.append(c);
			} else {
				if (builder.length() == 0 || builder.charAt(builder.length() - 1) != '_') {
					builder.append('_');
				}
			}
		}
		return builder.toString();
	}

	/* Getter/Setter */

	public String getId() {
		return id;
	}

	public ObjectProperty<Path> path() {
		return path;
	}

	public StringProperty mountName() {
		return mountName;
	}

	public StringProperty winDriveLetter() {
		return winDriveLetter;
	}

	public BooleanProperty unlockAfterStartup() {
		return unlockAfterStartup;
	}

	public BooleanProperty revealAfterMount() {
		return revealAfterMount;
	}

	public BooleanProperty usesIndividualMountPath() {
		return usesIndividualMountPath;
	}

	public StringProperty individualMountPath() {
		return individualMountPath;
	}

	public Optional<String> getIndividualMountPath() {
		if (usesIndividualMountPath.get()) {
			return Optional.ofNullable(Strings.emptyToNull(individualMountPath.get()));
		} else {
			return Optional.empty();
		}
	}

	public BooleanProperty usesReadOnlyMode() {
		return usesReadOnlyMode;
	}

	public StringProperty mountFlags() {
		return mountFlags;
	}
	
	public IntegerProperty pathLengthLimitation() {
		return pathLengthLimitation;
	}

	/* Hashcode/Equals */

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof VaultSettings && obj.getClass().equals(this.getClass())) {
			VaultSettings other = (VaultSettings) obj;
			return Objects.equals(this.id, other.id);
		} else {
			return false;
		}
	}

}
