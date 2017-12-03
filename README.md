RainOrNot
===================

A sample Android Weather App using two APIs :

 1. <a href="https://darksky.net">Dark Sky's</a> weather API for weather information of a place
 2. <a href="https://developers.google.com/places/android-api/autocomplete">Google's Places API</a> to search places on user input and link it to the latitude and longitude

----------


###Clone app source 
Fire up a terminal and type the following command :
```
git clone https://github.com/pramodbhadana/rainornot.git
```
or 
```
Open Android Studio 

Go to File --> New --> Project from Version Control --> Github 
 
Enter https://github.com/pramodbhadana/rainornot.git into the Git Repository URL field, follow other instructions and you are good to go.
```
----------

###Setting up the API keys 

To use the Dark Sky as well as the Google API, API keys are required. Head over to following links to generate one for you.

> **Note:**

> - Dark Sky --> https://darksky.net/dev
> - Google's Geo API --> https://developers.google.com/places/android-api/signup

#### <i class="icon-file"></i> Configuring project to use API keys 

The keys generated needs to be placed in specific file so that the project can access those.

/app/src/main/assets/privateKeys.txt

This file holds the keys.

```
dark_sky_key = add_your_darksky_key_here
google_geo_api_key = add_your_googlegeo_api_key_here
```

> **Tip:** The name google_geo could be misleading here. Actually it is Google Places API key.
####Add your keys to the above file.
------------
###Licence
This project is licensed under Apache Licence 2.0. Feel free to use it.

------------
###Credits

I used this beautiful <a href="https://www.uplabs.com/posts/material-design-weather-icon-set">weather icon set </a> by <a href="https://www.uplabs.com/kevinttob">Kevin Aguilar</a> for this project. Thanks to him for that.
