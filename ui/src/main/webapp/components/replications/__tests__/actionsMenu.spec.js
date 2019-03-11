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
