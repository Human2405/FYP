const firebaseConfig = {
    apiKey: "AIzaSyAPOBFqm5m_ZWHgmWfhQSmV8NLE8640Yr4",
    authDomain: "pass-427cc.firebaseapp.com",
    databaseURL: "https://pass-427cc-default-rtdb.asia-southeast1.firebasedatabase.app",
    projectId: "pass-427cc",
    storageBucket: "pass-427cc.appspot.com",
    messagingSenderId: "670503812237",
    appId: "1:670503812237:web:a3c6725694190f2ea1b6c4",
    measurementId: "G-NVS9W5BYJH"
  };
  
  // Initialize Firebase
  try{
    firebase.initializeApp(firebaseConfig);
  }
  catch(e){
    console.log(e);
  }