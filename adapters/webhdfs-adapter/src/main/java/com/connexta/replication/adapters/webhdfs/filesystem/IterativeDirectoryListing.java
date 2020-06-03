package com.connexta.replication.adapters.webhdfs.filesystem;

import com.fasterxml.jackson.annotation.JsonProperty;

public class IterativeDirectoryListing {

  @JsonProperty("DirectoryListing")
  DirectoryListing directoryListing;

  public DirectoryListing getDirectoryListing() {
    return directoryListing;
  }

  public void setDirectoryListing(DirectoryListing directoryListing) {
    this.directoryListing = directoryListing;
  }
}
