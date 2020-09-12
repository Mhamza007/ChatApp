'use strict'


const functions = require('firebase-functions');

const admin = require('firebase-admin');
admin.initializeApp(functions.config().firebase);

exports.sendNotification = functions.database.ref('/Notifications/{chatReceiverId}/{notification_id}')
    .onWrite((data, context) => {
             const chatReceiverId = context.params.chatReceiverId;
             const notification_id = context.params.notification_id;
             
             console.log('Send Notification to: ', chatReceiverId);

            if (!data.after.val()){
                console.log('Notification has been deleted: ', notification_id);
                return null;
            }
    
    const chatSenderId = admin.database().ref(`/Notifications/${chatReceiverId}/${notification_id}`).once('value');
    return chatSenderId.then(fromUserResult => {
        
        const fromSenderUserId = fromUserResult.val().from;
        console.log('Notification received from: ', fromSenderUserId);
        
        const userQuery = admin.database().ref(`/Users/${fromSenderUserId}/name`).once('value');
        return userQuery.then(userResult => {
            const senderUserName = userResult.val();
            console.log('sender name is: ', senderUserName);
        
        const deviceToken = admin.database().ref(`/Users/${chatReceiverId}/token`).once('value');

        console.log('Checkpoint');

        
        return deviceToken.then(result => {
            const tokenId = result.val();
    
            const payload = {
                notification : {
                    fromSenderUserId: fromSenderUserId,
                    title: "Message Received",
                    body: `A text message is received from ${senderUserName}`, 
                    icon: "default"
                }
            };
        
        return admin.messaging().sendToDevice(tokenId, payload).then(response => {
            console.log("This was notification");
            return null;
        }).catch(error => {
            console.error(error);
            res.error(500);
        });
        });
        });
    });
});
