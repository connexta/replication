/*global test, shallow, expect */
import React from 'react'
import Site from '../Site'

test('site renders correctly', () => {
  const wrapper = shallow(
    <Site name='example' content='www.example.com' id='1234' />
  )
  expect(wrapper.exists()).toBe(true)
})
