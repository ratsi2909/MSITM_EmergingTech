#!/usr/bin/env python
# coding: utf-8

# In[ ]:


def on_button_pressed_a():
    global steps
    steps = 0
    basic.show_number(steps)
    basic.show_string("Start Over!")
input.on_button_pressed(Button.A, on_button_pressed_a)

def on_gesture_shake():
    global steps
    if input.acceleration(Dimension.STRENGTH) > 1:
        steps += 1
        basic.show_number(steps)
input.on_gesture(Gesture.SHAKE, on_gesture_shake)

steps = 0
steps = 0
basic.show_string("Hello!")

def on_forever():
    if steps >= 10:
        basic.show_string("Hooray!")
basic.forever(on_forever)


# In[ ]:




