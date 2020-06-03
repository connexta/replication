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

public class DirectoryListing {

  //    private static final String PARTIAL_LISTING_KEY = "partialListing";
  //    private static final String REMAINING_ENTRIES_KEY = "remainingEntries";
  //    private static final String FILE_STATUSES_KEY = "FileStatuses";
  //    private static final String FILE_STATUS_KEY = "FileStatus";

  //    private List<FileStatus> partialListing;
  //    private int remainingEntries = 0;

  //    public DirectoryListing(Map<String, Object> directoryListing) {

  //        this.partialListing = getPartialListing((Map)
  // directoryListing.get(PARTIAL_LISTING_KEY));
  //
  //        this.remainingEntries = (Integer) directoryListing.get(REMAINING_ENTRIES_KEY);
  //    }

  //    private List<FileStatus> getPartialListing(Map<String, Object> partialListing) {
  //
  //        return getFileStatuses((Map) partialListing.get(FILE_STATUSES_KEY));
  //
  //    }
  //
  //    private List<FileStatus> getFileStatuses(Map<String, Object> fileStatuses) {
  //
  //        List<FileStatus> files = new ArrayList<>();
  //
  //        for(Map<String, Object> file : (ArrayList<Map<String, Object>>)
  // fileStatuses.get(FILE_STATUS_KEY)) {
  //            files.add(new FileStatus(file));
  //        }
  //        return files;
  //
  //    }

  PartialListing partialListing;
  int remainingEntries;

  public PartialListing getPartialListing() {
    return partialListing;
  }

  public void setPartialListing(PartialListing partialListing) {
    this.partialListing = partialListing;
  }

  public int getRemainingEntries() {
    return remainingEntries;
  }

  public void setRemainingEntries(int remainingEntries) {
    this.remainingEntries = remainingEntries;
  }
}
