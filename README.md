# Rest_YOLO

Implementation of YOLO CNN running in a REST API. By making calls of the json format {"link": "url_to_image"} to the specified controller, 
the application will download the image, and run the network on the image, returning a json of form 
```json
{
  "item": "likelihood",
  "coord":"",
  "coord":"",
  "coord":"",
  "coord":""
}
```

* Requirements
To get the program running you will need darknet already installed along with the associated weight and config files. The program will need to
be launched from the same folder as darknet. You will also need to edit the src/image.c of darknet. You will need to edit the function of draw_detections.
You have to uncomment or add the line 
```c++
printf("%f %f %f %f\n", b.x, b.y, b.w, b.h)
```
as well as remove any newline feeds of the printfs in this file. 
You will also need to add two printfs of a newline return. One right after the variables are declared, and another one at the end of the function 
but still inside the first for loop.

This is due to design choices on my part of not wanting to restart the network for efficiency purposes.

