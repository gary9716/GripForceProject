# Project Data:

* all data will be placed under the Project Folder(named GripForce) which is located under the root of sdcard folder. 

* System SD Card Folder: /storage/sdCard0

* Removable SD Card Folder: /storage/extSdCard

* the app will try to search the Removable SD Card first. if not existed, it will use System SD Card.

# Data Naming and Format:
<ol> <li> For Grip Force Logs: </li>

* Naming: GripForce_{UserID}.txt

* Format:{timestamp in milliseconds since experiment view loaded},{sensor strip 1 data point 1},{sensor strip 1 data point 2}, ... {sensor strip 1 data point n},{sensor strip 2 data point 1} ... {sensor strip 2 data point n} ... {sensor strip m data point 1} ... {sensor strip m data point n}

* Path: GripForce/Logs

<li> For Tip Force Logs: </li>

* Naming: TipForce_{UserID}_{Grade}_{The order of a character in a file}.txt

* Format:{timestamp in milliseconds since experiment view loaded},{x coordinate},{y coordinate},{samsung note compatiable pen tip force}

* Path: GripForce/Logs

<li> Handwriting Images: </li>

* Naming: {UserID}_{Grade}_{The order of a character in a file}.png

* Path: GripForce/Images

<li> User Personal Information: </li>

* Naming: {UserID}.txt

* Path: GripForce/PersonalInformation

</ol>
# Testing Example Characters:

* Naming: Grade_{Grade}_Characters.txt

 * ex: Grade_1_Characters.txt means first grade

* Encoding: UTF-8

* Format: {Field Name} : {Field data}

* Path: GripForce/Example_Characters

# Other Configurable Setting:

- Chinese Characters Font:

 * The font of Chinese Example Characters in experiment page can be changed. Just put the font file(.ttf) under the path {System SD Card Path}/GripForce/ and renamed as "chinese_exp.ttf".

 * If you wish to use otf file , please refer to the file named ExperimentActivity.java and search "Typeface" modify the code and compile for your own purpose.   

# Troubleshooting:

1. If you want to reset user ID, use System Application Manager to clean the app setting

2. The features of hiding and showing system bar require the system permission. So the features are disabled if the android device haven't been rooted.

# Set up this project:(For Developers)

* you will need to add "libraryProject" under the root path of this project as a library project in order to successfully compile this project.