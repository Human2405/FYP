let username = document.getElementById('username');
let password = document.getElementById('password');
let loginButton = document.getElementById('loginButton');
let logoutButton = document.getElementById('logoutButton');

loginButton.addEventListener('click', function(){
    login();
})

logoutButton.addEventListener('click', function(){
    logout();
})

/*function login(){
    username = username.value;
    password = password.value;

    console.log(username + password);
    firebase.auth().signInWithEmailAndPassword(username, password)
}*/

function login(){
    username = username.value;
    password = password.value;


    var evidence = generateEvidence(username, password);

    // Send evidence and username to the server for verification
    fetch('verify_login.php', { // point to  PHP script
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({ username: username})
        //body: JSON.stringify({ username: username, evidence: evidence })
    })
    .then(response => {
        if (response.ok) {
            console.log('Login successful');

            firebase.auth().signInWithEmailAndPassword(username, password)
            /*.then((userCredential) => {
                // Signed in successfully, get the user's UID
                var user = userCredential.user;
                var uid = user.uid;
        
                var usersRef = firebase.firestore().collection('users');
                
                // Retrieve the user's phone number from Firestore
                usersRef.doc(uid).get()
                    .then((doc) => {
                        if (doc.exists) {
                            // Get the phone number from the document data
                            var phoneNumber = doc.data().phoneNumber;
                            
                            console.log(phoneNumber);
                            // Initiate phone number authentication
                            var phoneNumberAuthProvider = new firebase.auth.PhoneAuthProvider();
                            var appVerifier = new firebase.auth.RecaptchaVerifier('recaptcha-container');
                            console.log("appVerifier: " + appVerifier);

                            phoneNumberAuthProvider.verifyPhoneNumber(
                                phoneNumber,
                                appVerifier,
                                new firebase.auth.RecaptchaVerifier('recaptcha-container'), // You can replace 'recaptcha-container' with the ID of your reCAPTCHA container
                                firebase.auth().signInWithPhoneNumber(phoneNumber, appVerifier)
                                    .then((confirmationResult) => {
                                        // SMS sent. Prompt user to type the code from the message, then sign in with the confirmation code.

                                        var code = prompt('Enter the verification code:', '');
                                        sendNotification('New Login Detected!');
                                        confirmationResult.confirm(code)
                                            .then(() => {
                                                // Phone number authentication successful
                                                console.log('Phone number authentication successful');
                                                // Call the function to retrieve passwords
                                                console.log("user id is: " + uid);
                                                retrievePasswords(uid);
                                            }).catch((error) => {
                                                // Verification code entered incorrectly
                                                console.error('Error verifying code:', error);
                                            });
                                    }).catch((error) => {
                                        // SMS not sent
                                        console.error('Error sending SMS:', error);
                                    })
                            );
                        } else {
                            console.error('User document not found');
                        }
                    }).catch((error) => {
                        console.error('Error getting document:', error);
                    });
            }).catch((error) => {
                // Failed to sign in with email and password
                console.error('Error signing in:', error);
           });*/
            
        } else {
            console.error('Login failed');
        }
    })
    .catch(error => {
        console.error('Error during login:', error);
    });

    console.log(username + password);
    console.log(username + password);
    //firebase.auth().signInWithEmailAndPassword(username, password)
}


/*function login() {
    let usernameInput = document.getElementById('username'); // Assuming an input field with ID 'username'
    let passwordInput = document.getElementById('password'); // Assuming an input field with ID 'password'

    let username = usernameInput.value;
    let password = passwordInput.value;

    // Reference to the Firestore users collection
    var usersRef = firebase.firestore().collection('users');

    // Check if a user with the given email (username) exists in Firestore
    usersRef.where('email', '==', username).get()
        .then(snapshot => {
            if (snapshot.empty) {
                console.error('Login failed: User does not exist');
                return; // Stop execution if user does not exist
            }

            // User exists, proceed with sending evidence and username to the server for verification
            fetch('verify_login.php', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ username: username })
            })
            .then(response => {
                if (response.ok) {
                    console.log('Login successful');
                    // Perform further actions like Firebase Auth sign-in, etc.
                } else {
                    console.error('Login failed');
                }
            })
            .catch(error => {
                console.error('Error during login:', error);
            });

        })
        .catch(error => {
            console.error('Error checking user existence:', error);
        });

    console.log(username + " " + password);
}*/





function generateEvidence(username) {
    const proofEvident = btoa(username);
    return proofEvident;
}


function logout() {
    firebase.auth().signOut().then(function() {
      // Sign-out successful.
      console.log("User signed out");
    }).catch(function(error) {
      // An error happened.
      console.error("Error signing out: ", error);
    });
  }
  

/*firebase.auth().onAuthStateChanged((user)=>{
    if(user){
        console.log("User logged in from AuthChange");
        var user = firebase.auth().currentUser;
        var uid = user.uid;
        sendNotification('New Login Detected!');
        retrievePasswords();

    } else{
        //code
        console.log("User not logged in");
    }
})*/

firebase.auth().onAuthStateChanged((user) => {
    if (user) {
        console.log("User logged in from AuthChange");
        var uid = user.uid;
        var otp = generateOTP(); // Generate OTP
        sendOTPToUser(otp); // Send OTP to user's phone
        promptOTPVerification(otp); // Prompt user to enter OTP for verification
    } else {
        // Code for user not logged in
        console.log("User not logged in");
    }
});

// Function to generate a random 6-digit OTP
function generateOTP() {
    return Math.floor(100000 + Math.random() * 900000);
}

// Function to send OTP to user's phone
function sendOTPToUser(otp) {
    sendNotification('Your OTP is: ' + otp); // Example notification
}

// Function to prompt user to enter OTP for verification
function promptOTPVerification(generatedOTP) {
    var userInput = prompt("Please enter the OTP sent to your phone:");

    if (userInput === generatedOTP.toString()) {
        // User entered correct OTP, retrieve passwords
        retrievePasswords();
    } else {
        // Incorrect OTP, throw error to user
        alert("Incorrect OTP. Please try again.");
    }
}

/*function retrievePasswords(uid) {

    // Reference to the 'passwords' collection
    var passwordsRef = firebase.firestore().collection('passwords');

    // Reference to the 'user_passwords' collection under the specific user's document
    var userPasswordsCollection = passwordsRef.doc(uid).collection('user_passwords');


    // Query passwords associated with the user's UID
   userPasswordsCollection.get()
        .then((querySnapshot) => {
            var passwordsContainer = document.getElementById('passwords-container');

            // Clear previous content
            passwordsContainer.innerHTML = '';

            querySnapshot.forEach((doc) => {
                var data = doc.data();
                var userName = data.webUser;
                var userPassword = data.webPass;
                var websiteName = data.webName;
                var webkey = data.web;

                const decodedUserName = decryptPass(userName, webkey);
                const decodedPass = decryptPass(userPassword, webkey);
                const decodedwebName = decryptPass(websiteName, webkey);

                 // Create HTML elements to display password information
                 var passwordElement = document.createElement('div');
                 passwordElement.classList.add('password-card');
                 passwordElement.innerHTML = `
                     <p><strong>Website Name:</strong> ${decodedwebName}</p>
                     <p><strong>Username:</strong> ${decodedUserName}</p>
                     <p><strong>Password:</strong> ${decodedPass}</p>
                 `;
             
                 // Append to the container
                 passwordsContainer.appendChild(passwordElement);

                //console.log("user:" + data.webUser + "pass:" + data.webPass + "name:" + data.webName + "key:" + data.web)
                console.log("user is " + decodedUserName + " pass is " + decodedPass + " web is " + decodedwebName + " key is " + webkey)
               
            });
        })
        .catch((error) => {
            console.error('Error retrieving passwords:', error);
        });
}*/

/*firebase.auth().onAuthStateChanged(async (user) => {
    if (user) {
        console.log("User logged in");

        // Get the user's FCM token
        const userId = user.uid;
        const userDocRef = doc(db, "users", userId);

        try {
            const userDocSnapshot = await getDoc(userDocRef);
            const userFCMToken = userDocSnapshot.data().fcmToken;

            // Now you can use userFCMToken for further processing or sending notifications
            console.log("User FCM Token:", userFCMToken);

            // Send FCM Notification
            const message = {
                notification: {
                    title: "Notification Title",
                    body: "Notification Body",
                },
                token: "c1olSFjgRNWlrpO3VLnWFi:APA91bFJOz4kToOwdCXUePUcT3Vs4Uy0qwFe4f6yKeq5kaaGJJlJQYpnP0_Fi2XZzrBE9cFq--TMzhGYXj_ykRJT3e5kffDW_wNLmX-R7c0VqqgYPXmU8EFNT_4W6ZDK4PtljIElUBhr",
            };

            try {
                const response = await fetch("https://fcm.googleapis.com/fcm/send", {
                    method: "POST",
                    headers: {
                        Authorization: "AAAAnB0kPI0:APA91bFmVcFIToRz_H5vXCqeLO_1tb3BeQnUHKKid7msetmMXsl2Jqq58HxzdIHfr9YC-sIV8qOp3p6zxHAnw3I7rdKhb92ZteNgaBkJlC9v-fsTo8QwT3_qrhNDdXoJ3HIZ8C8YxGFQ", // Replace with your server key
                        "Content-Type": "application/json",
                    },
                    body: JSON.stringify(message),
                });

                const result = await response.json();
                console.log("Notification sent successfully:", result);
            } catch (error) {
                console.error("Error sending notification:", error);
            }

        } catch (error) {
            console.error("Error getting user data:", error);
        }

    } else {
        // User not logged in code
        console.log("User not logged in");
    }
});*/

function retrievePasswords() {
    var user = firebase.auth().currentUser;

    if (user) {
        var uid = user.uid;

        // Reference to the passwords collection
        //var passwordsRef = firebase.firestore().collection('passwords');

        // Reference to the 'passwords' collection
        var passwordsRef = firebase.firestore().collection('passwords');

        // Reference to the 'user_passwords' collection under the specific user's document
        var userPasswordsCollection = passwordsRef.doc(uid).collection('user_passwords');


        // Query passwords associated with the user's UID
       userPasswordsCollection.get()
            .then((querySnapshot) => {
                var passwordsContainer = document.getElementById('passwords-container');

                // Clear previous content
                passwordsContainer.innerHTML = '';

                querySnapshot.forEach((doc) => {
                    var data = doc.data();
                    var userName = data.webUser;
                    var userPassword = data.webPass;
                    var websiteName = data.webName;
                    var webkey = data.web;

                    /*var decodedUserName = unCaesar(webkey);
                    var realUserName = translate(decodedUserName)(userName);
                    var decodedPass = unCaesar(webkey);
                    var realPass = translate(decodedPass)(userPassword);
                    var decodedwebName = unCaesar(webkey)
                    var realWebName = translate(decodedwebName)(websiteName)*/

                    const decodedUserName = decryptPass(userName, webkey);
                    const decodedPass = decryptPass(userPassword, webkey);
                    const decodedwebName = decryptPass(websiteName, webkey);

                     // Create HTML elements to display password information
                     var passwordElement = document.createElement('div');
                     passwordElement.classList.add('password-card');
                     passwordElement.innerHTML = `
                         <p><strong>Website Name:</strong> ${decodedwebName}</p>
                         <p><strong>Username:</strong> ${decodedUserName}</p>
                         <p><strong>Password:</strong> ${decodedPass}</p>
                     `;
                 
                     // Append to the container
                     passwordsContainer.appendChild(passwordElement);

                    //console.log("user:" + data.webUser + "pass:" + data.webPass + "name:" + data.webName + "key:" + data.web)
                    console.log("user is " + decodedUserName + " pass is " + decodedPass + " web is " + decodedwebName + " key is " + webkey)
                   // Create HTML elements to display password information
                   /* var passwordElement = document.createElement('div');
                    passwordElement.innerHTML = `
                        <p><strong>Website Name:</strong> ${data.website}</p>
                        <p><strong>Username:</strong> ${data.username}</p>
                        <p><strong>Password:</strong> ${data.password}</p>
                        <hr>
                    `;

                    // Append to the container
                    passwordsContainer.appendChild(passwordElement);*/
                });
            })
            .catch((error) => {
                console.error('Error retrieving passwords:', error);
            });
    } else {
        console.warn('User not logged in');
    }
}

function decryptPass(text, key) {
    return Array.from(text, char => {
        if (/[A-Za-z]/.test(char)) {
            const base = char === char.toUpperCase() ? 'A' : 'a';
            return String.fromCharCode(((char.charCodeAt(0) - base.charCodeAt(0) - key + 26) % 26) + base.charCodeAt(0));
        } else if (/[0-9]/.test(char)) {
            return String.fromCharCode(((char.charCodeAt(0) - '0'.charCodeAt(0) - key + 10) % 10) + '0'.charCodeAt(0));
        } else {
            return char;
        }
    }).join('');
}


function sendNotification(message){
    //var request = 	require('request');

        var restKey = 'NjczYjgwNDYtYjIxNi00ZmQzLWFjZmMtNTYzMzc2N2U2NGU3';
        var appID = '1906e057-2ac0-426a-a415-b7849565fdd8';
        fetch('https://onesignal.com/api/v1/notifications', {
            method: 'POST',
            headers: {
                "authorization": "Basic " + restKey,
                "content-type": "application/json"
            },
            body: JSON.stringify({
                'app_id': appID,
                'contents': { en: message },
                'included_segments': ['All'],
            })
        })
        .then(response => response.json())
        .then(data => {
            if (!data.errors) {
                console.log(data);
            } else {
                console.error('Error:', data.errors);
            }
        })
        .catch(error => {
            console.error('Error sending notification:', error);
        });
    
}

// Function to send a push notification to a specific user
/*function sendNotificationToUser(userId) {
    // Retrieve the FCM token from Firestore based on the user's ID
    firebase.firestore().collection('users').doc(userId).get()
        .then((doc) => {
            if (doc.exists) {
                const userToken = doc.data().token;

                // Construct the notification payload
                const notification = {
                    to: userToken,
                    notification: {
                        title: 'Notification Test',
                        body: 'Your custom notification message.',
                        // Add more properties as needed
                    },
                };

                // Send the notification using Firebase Cloud Messaging API
                fetch('https://fcm.googleapis.com/fcm/send', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                        'Authorization': 'AAAAnB0kPI0:APA91bFmVcFIToRz_H5vXCqeLO_1tb3BeQnUHKKid7msetmMXsl2Jqq58HxzdIHfr9YC-sIV8qOp3p6zxHAnw3I7rdKhb92ZteNgaBkJlC9v-fsTo8QwT3_qrhNDdXoJ3HIZ8C8YxGFQ', // Replace with your server key
                    },
                    body: JSON.stringify(notification),
                })
                .then(response => response.json())
                .then(data => {
                    console.log('Notification sent successfully:', data);
                })
                .catch(error => {
                    console.error('Error sending notification:', error);
                });
            } else {
                console.error('User document not found in Firestore.');
            }
        })
        .catch((error) => {
            console.error('Error retrieving user document from Firestore:', error);
        });
}*/

