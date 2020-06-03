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

public class FileStatus {

  private Date accessTime;
  private int blockSize;
  private int childrenNum;
  private int fileId;
  private String group;
  private int length;
  private Date modificationTime;
  private String owner;
  private String pathSuffix;
  private String permission;
  private int replication;
  private int storagePolicy;
  private String type;

  public Date getAccessTime() {
    return accessTime;
  }

  public void setAccessTime(Date accessTime) {
    this.accessTime = accessTime;
  }

  public int getBlockSize() {
    return blockSize;
  }

  public void setBlockSize(int blockSize) {
    this.blockSize = blockSize;
  }

  public int getChildrenNum() {
    return childrenNum;
  }

  public void setChildrenNum(int childrenNum) {
    this.childrenNum = childrenNum;
  }

  public int getFileId() {
    return fileId;
  }

  public void setFileId(int fileId) {
    this.fileId = fileId;
  }

  public String getGroup() {
    return group;
  }

  public void setGroup(String group) {
    this.group = group;
  }

  public int getLength() {
    return length;
  }

  public void setLength(int length) {
    this.length = length;
  }

  public Date getModificationTime() {
    return modificationTime;
  }

  public void setModificationTime(Date modificationTime) {
    this.modificationTime = modificationTime;
  }

  public String getOwner() {
    return owner;
  }

  public void setOwner(String owner) {
    this.owner = owner;
  }

  public String getPathSuffix() {
    return pathSuffix;
  }

  public void setPathSuffix(String pathSuffix) {
    this.pathSuffix = pathSuffix;
  }

  public String getPermission() {
    return permission;
  }

  public void setPermission(String permission) {
    this.permission = permission;
  }

  public int getReplication() {
    return replication;
  }

  public void setReplication(int replication) {
    this.replication = replication;
  }

  public int getStoragePolicy() {
    return storagePolicy;
  }

  public void setStoragePolicy(int storagePolicy) {
    this.storagePolicy = storagePolicy;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }
}
