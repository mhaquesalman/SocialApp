'user-strict'

const functions = require('firebase-functions');
const admin = require('firebase-admin');
// admin.initializeApp();
admin.initializeApp(functions.config().firebase);

exports.sendNotification = functions.database.ref('chats/{chat_id}').onWrite((change, context) => {

    // chat id
    const chat_id = context.params.chat_id;
    console.log("Chat id: " + chat_id);

    const deviceRef = admin.database().ref().child('chats').child(chat_id).once('value');
    return deviceRef.then(queryResult => {

        // sender id
        const from_userId = queryResult.val().sender;
        // message 
        const from_message = queryResult.val().message;
        // receiver id
        const to_userId = queryResult.val().receiver;
        // image or not
        const is_image = queryResult.val().image;

        var imageVal = "";
        var notificationBody = "";
        if(is_image) {
            imageVal = "true";
            notificationBody = "You have received a photo";
        } else {
            imageVal = "false";
            notificationBody = from_message;
        }
        
        console.log("Image available: " + imageVal);
        console.log("Notificationbody: " + notificationBody);


        // sender reference
        const from_data = admin.database().ref("fusers/" + from_userId).once('value')
        // receiver reference
        const to_data = admin.database().ref("fusers/" + to_userId).once('value')

        return Promise.all([from_data, to_data]).then(result => {

            const from_name = result[0].val().name;
            const to_name = result[1].val().name;
            const to_token = result[1].val().token;

            console.log("From: " + from_name + " To: " + to_name);

            const payload = {
                notification: {
                    title: "Message From: " + from_name,
                    body: notificationBody,
                    icon: "default",
                    click_action: "com.salman.socialapp.NOTIFICATION"
                },
                data: {
                    message: from_message,
                    image: imageVal,
                    from_user_id: from_userId
                }
            };

            return admin.messaging().sendToDevice(to_token, payload).then(result => {
                console.log("Notification Sent");
                return result;
            })

        })

    })

})
