# UR-EYE

CSIS: 4495 Applied Research Project
PROJECT NAME: <b>UR EYE</b>

<b>Project Contributors</b>
- Mili Modi
- Prudhvi charan thirumal reddy
- Naga Sudheer Bolla

Our project idea has evolved from a serious real world problem, where we do observe in daily life about how the blind people face problems with simple things in day to day life.
We aim to make their life a little bit easy, by building an Android application for assisting the blind people as much as possible using the technology.

We will create a custom camera which focuses on the following features.
1. Object detection: when walking or standing, warns user if they are going too close or crash into something or something is approaching them.
2. Text Detection: Detecting the text from images, books or sign boards and reads it out.
3. Face detection and facial expression capture: Captures the facial expression of the person it is pointed at, and lets the users know their expression as they can’t find it 
out with their own eyes.

We will enable voice recognition command from user to perform the in-app functions along with touch features. We will try to implement the camera to run in the background even
when screen is locked as it might help with battery performance and efficiency.

Tech Stack is Android using Machine Learning feature in Firebase or OCR reader. 

Our App will activate voice recognition listening whenever user touches at any place in our app. Its for easy assistance. Once user touches anywhere, it will listen for 
instructions. We will guide the users initially though a guided tour of how to use our app, access voice command codes etc. 

Main or Launch Screen will have four options:
1.	Travelling Mode,
2.	Reader Mode,
3.	Meeting Mode.
4.	Saved.

User can simply say either 1, 2, 3, 4 or Travel, Read, Meeting, Saved etc… to select the option he/she wants. We can include multiple commands for it while implementation.
To exit from current screen, they need to say exit screen command.

1.	<b>Travel Mode</b>: If user selects this mode, we will start camera and start detecting objects. If we detect any object too close to the user or accelerating towards them,
    we will warn them. We will use Object detection mechanism for it and have to create custom algorithm for detecting the speed etc.. We will have an option to save the route 
    they travelled for future usage.
2.	<b>Reader Mode</b>: If user selects this mode and points screen at anything like a book or board or image, we will detect the text using Text Detection SDK and read out the 
    text. We will have an option to save the text into local db, from where they can access it later.
3.	<b>Meeting Mode</b>: If user selects this mode, we will open MLKIT SDK from firebase and detects the facial expression and say their expression.
4.	<b>Saved</b>: This will have saved paths and text, where users can access them.

<b>Experimental Thought</b>: We might add location tracking and emergency alert feature as well. But for this, we might need setup for backend server and all. So we are still
                             in discussion, whether to implement it or not. We will update you soon with this feature as well. For now, this is our base idea for project.





