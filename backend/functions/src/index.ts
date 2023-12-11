/**
 * Import function triggers from their respective submodules:
 *
 * import {onCall} from "firebase-functions/v2/https";
 * import {onDocumentWritten} from "firebase-functions/v2/firestore";
 *
 * See a full list of supported triggers at https://firebase.google.com/docs/functions
 */

import {initializeApp} from "firebase-admin/app";
import {getFirestore} from "firebase-admin/firestore";
import {onRequest} from "firebase-functions/v2/https";
import * as logger from "firebase-functions/logger";

initializeApp();

const db = getFirestore();


// Start writing functions
// https://firebase.google.com/docs/functions/typescript

export const helloWorld = onRequest((request, response) => {
  logger.info("Hello logs!", {structuredData: true});
  response.send({data: {message: "Hello world!"}});
});


export const setupDatabase = onRequest(async (request, response) => {
  const username = request.body.data.username;

  db
    .collection("SensorsData")
    .doc(username)
    .set({
      [username]: username,
    });
});

export const getDataFeedback = onRequest(async (request, response) => {
  try {
    const email = request.body.data.username;
    const userDocRef = db.collection("SensorsData").doc(email);

    const userDateRef = userDocRef.collection(email);

    const dateSnapshot = await userDateRef.get();

    if (dateSnapshot.empty) {
      response.send({
        data: {
          heartRate: {
            current: 0.0,
            resting: 0.0,
            high: 0.0,
          },
          temperature: {
            current: 0.0,
            resting: 0.0,
            high: 0.0,
          },
        },
      });
      return;
    }

    const documents = dateSnapshot.docs.map((doc) => doc.data());

    logger.info("documents", JSON.stringify(documents));
    /*
    const userData = {};
    dateSnapshot.forEach(async (dateDoc) => {
      const entryId = dateDoc.id;
      const entryData = dateDoc.data();

      const userHourRef = userDateRef.doc(entryId);
      const hourSnapshot = await getDocs(userHourRef);

      const hourData = {};
      hourSnapshot.forEach((hourDoc) => {
        const hourId = hourDoc.id;
        const hourData = hourDoc.data();
        hourData[hourId] = hourData;
      });
      userData[entryId] = { ...entryData, hour: hourData };

    });
    */
  } catch (e) {
    logger.error(e);
  }

  // logger.info('Data', {structuredData: true, userData});
  // response.send({ data: { userData } });
});

