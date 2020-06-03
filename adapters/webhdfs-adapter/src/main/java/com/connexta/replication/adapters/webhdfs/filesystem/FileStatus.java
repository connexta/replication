/**
 * Copyright (c) Connexta
 *
 * <p>This is free software: you can redistribute it and/or modify it under the terms of the GNU
 * Lesser General Public License as published by the Free Software Foundation, either version 3 of
 * the License, or any later version.
 *
 * <p>This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details. A copy of the GNU Lesser General Public
 * License is distributed along with this program and can be found at
 * <http://www.gnu.org/licenses/lgpl.html>.
 */
package com.connexta.replication.adapters.webhdfs.filesystem;

import java.util.Date;
import java.util.Map;

public class FileStatus {

    private final Date accessTime;
    private final int blockSize;
    private final int childrenNum;
    private final int fileId;
    private final String group;
    private final int length;
    private final Date modificationTime;
    private final String owner;
    private final String pathSuffix;
    private final String permission;
    private final int replication;
    private final int storagePolicy;
    private final String type;

    public FileStatus(Map<String, Object> file) {

        this.accessTime = (Date) file.get("accessTime");
        this.blockSize = (int) file.get("blockSize");
        this.childrenNum = (int) file.get("childrenNum");
        this.fileId = (int) file.get("fileId");
        this.group = (String) file.get("group");
        this.length = (int) file.get("length");
        this.modificationTime = (Date) file.get("modificationTime");
        this.owner = (String) file.get("owner");
        this.pathSuffix = (String) file.get("pathSuffix");
        this.permission = (String) file.get("permission");
        this.replication = (int) file.get("replication");
        this.storagePolicy = (int) file.get("storagePolicy");
        this.type = (String) file.get("type");
    }

    public Date getAccessTime() {
        return accessTime;
    }

    public int getBlockSize() {
        return blockSize;
    }

    public int getChildrenNum() {
        return childrenNum;
    }

    public int getFileId() {
        return fileId;
    }

    public String getGroup() {
        return group;
    }

    public int getLength() {
        return length;
    }

    public Date getModificationTime() {
        return modificationTime;
    }

    public String getOwner() {
        return owner;
    }

    public String getPathSuffix() {
        return pathSuffix;
    }

    public String getPermission() {
        return permission;
    }

    public int getReplication() {
        return replication;
    }

    public int getStoragePolicy() {
        return storagePolicy;
    }

    public String getType() {
        return type;
    }
}
