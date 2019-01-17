import React from "react";
import Site from "./Site";

test("site renders correctly", () => {
  const wrapper = mount(
    <Site name="example" connected={true} onClick={() => {}} />
  );
  expect(wrapper.exists()).toBe(true);
});

test("site connected renders power icon", () => {
  const wrapper = mount(
    <Site name="example" connected={true} onClick={() => {}} />
  );
  expect(wrapper.find("PowerIcon").length).toBe(1);
  expect(wrapper.find("PowerOffIcon").length).toBe(0);
});

test("site not connected renders power off icon", () => {
  const wrapper = mount(
    <Site name="example" connected={false} onClick={() => {}} />
  );
  expect(wrapper.find("PowerOffIcon").length).toBe(1);
  expect(wrapper.find("PowerIcon").length).toBe(0);
});
