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

/*global test, shallow, expect */
import React from 'react'
import ActionsMenu from '../ActionsMenu'
import Replications from '../replications'

test('empty replication property causes no render', () => {
  const wrapper = shallow(
    <ActionsMenu
      replication={undefined}
      menuId='1234'
      anchorEl={null}
      onClose={() => {}}
    />
  )
  expect(wrapper.isEmptyRender())
})

test('push in progress status renders cancel action', () => {
  const wrapper = shallow(
    <ActionsMenu
      replication={{
        id: '1234',
        replicationStatus: Replications.Status.PUSH_IN_PROGRESS,
        suspended: false,
      }}
      menuId='1234'
      anchorEl={null}
      onClose={() => {}}
    />
  )
  expect(wrapper.find('CancelReplication').exists()).toBe(true)
})

test('pull in progress status renders cancel action', () => {
  const wrapper = shallow(
    <ActionsMenu
      replication={{
        id: '1234',
        replicationStatus: Replications.Status.PULL_IN_PROGRESS,
        suspended: false,
      }}
      menuId='1234'
      anchorEl={null}
      onClose={() => {}}
    />
  )
  expect(wrapper.find('CancelReplication').exists()).toBe(true)
})

test('pending status renders cancel action', () => {
  const wrapper = shallow(
    <ActionsMenu
      replication={{
        id: '1234',
        replicationStatus: Replications.Status.PENDING,
        suspended: false,
      }}
      menuId='1234'
      anchorEl={null}
      onClose={() => {}}
    />
  )
  expect(wrapper.find('CancelReplication').exists()).toBe(true)
})

test('suspended config renders enable action', () => {
  const wrapper = shallow(
    <ActionsMenu
      replication={{
        id: '1234',
        replicationStatus: Replications.Status.PENDING,
        suspended: true,
      }}
      menuId='1234'
      anchorEl={null}
      onClose={() => {}}
    />
  )
  expect(wrapper.find('SuspendReplication').prop('suspend')).toBe(false)
})

test('enabled config renders suspend action', () => {
  const wrapper = shallow(
    <ActionsMenu
      replication={{
        id: '1234',
        replicationStatus: Replications.Status.PENDING,
        suspended: false,
      }}
      menuId='1234'
      anchorEl={null}
      onClose={() => {}}
    />
  )
  expect(wrapper.find('SuspendReplication').prop('suspend')).toBe(true)
})
