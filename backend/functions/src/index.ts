/**
 * Import function triggers from their respective submodules:
 *
 * import {onCall} from "firebase-functions/v2/https";
 * import {onDocumentWritten} from "firebase-functions/v2/firestore";
 *
 * See a full list of supported triggers at https://firebase.google.com/docs/functions
 */

import { initializeApp } from "firebase-admin/app";
import * as admin from "firebase-admin";
import { getFirestore } from "firebase-admin/firestore";
import { onRequest } from "firebase-functions/v2/https";
import * as logger from "firebase-functions/logger";

initializeApp();

const db = getFirestore();


// Start writing functions
// https://firebase.google.com/docs/functions/typescript

export const helloWorld = onRequest((request, response) => {
  logger.info("Hello logs!", { structuredData: true });
  response.send({ data: { message: "Hello world!" } });
});


export const setupDatabase = onRequest(async (request, response) => {
  const username = request.body.data.username;
  db
    .collection("SensorsData")
    .doc(username)
    .set({
      [username]: username,
    });

  db
    .collection(username)
    .doc("1970-01-01")
    .set({
      nothing: "nothing",
    });
});

function todaysDocumentId(): string {
  const today = new Date();
  const dd = String(today.getDate()).padStart(2, "0");
  const mm = String(today.getMonth() + 1).padStart(2, "0");
  const yyyy = today.getFullYear();

  return yyyy + "-" + mm + "-" + dd;
}

export const sendData = onRequest(async (request, response) => {
  const { email, temperature, heartRate,
    breathRate, moving, barking, day, timestamp, } = request.body.data;

  const id = day;
  logger.info("DATA", request.body.data);
  try {
    await db.collection(email)
      .doc(id)
      .update({
        data: admin.firestore.FieldValue.arrayUnion({
          temperature, heartRate, breathRate, moving, barking, timestamp
        })
      })
  } catch (e) {
    logger.error("error", e)
  }
});


function maxTemperature(registers: any[]) {
  return Math.max(...registers.map((reg: any) => reg.temperature))
}

function restingTemperature(registers: any[]) {
  const restingRegisters = registers
    .filter((reg: any) => reg.moving === false);
  
  if (restingRegisters.length === 0) {
    return 0;
  }
  
  const totalTemperature = restingRegisters
    .reduce((total, curr) => total + curr.temperature, 0);
  return totalTemperature / restingRegisters.length;
}

function maxHeartRate(registers: any[]) {
  return Math.max(...registers.map((reg: any) => reg.heartRate))
}

function restingHeartRate(registers: any[]) {
  const restingRegisters = registers
    .filter((reg: any) => reg.moving === false);
  
  if (restingRegisters.length === 0) {
    return 0;
  }
  
  const totalHeartRate = restingRegisters
    .reduce((total, curr) => total + curr.heartRate, 0);
  return totalHeartRate / restingRegisters.length;
}

export const getDataFeedback = onRequest(async (request, response) => {
  logger.info("ENTER");
  try {
    const email = request.body.data.username;
    logger.info("EMAIL", email);
    const id = todaysDocumentId();
    logger.info("ID", id);
    const doc = (await db.collection(email)
      .doc(id)
      .get()).data();

    if (doc === undefined) {
      response.send({
        data: {
          temperature: {
            current: 0.0,
            high: 0.0,
            resting: 0.0,
          },
          heartRate: {
            current: 0.0,
            high: 0.0,
            resting: 0.0,
          },
        }
      });
      return;
    }

    const registers = doc.data || [];
    const oneHour = 60 * 60 * 1000;
    const lastHourRegisters = registers
      .map((reg: any) => ({ ...reg, timestamp: new Date(Date.parse(reg.timestamp)) }))
      .filter((reg: any) => ((new Date()).getTime() - reg.timestamp.getTime()) < oneHour)

    response.send({
      data: {
        temperature: {
          current: lastHourRegisters.at(-1).temperature,
          high: maxTemperature(lastHourRegisters),
          resting: restingTemperature(lastHourRegisters),
        },
        heartRate: {
          current: lastHourRegisters.at(-1).heartRate,
          high: maxHeartRate(lastHourRegisters),
          resting: restingHeartRate(lastHourRegisters),
        },
      }
    })

    logger.info("DOCUMENTS", doc);
  } catch (e) {
    logger.error(e);
    response.status(500).send("Internal Server Error");
  }
});

export const getLastHourData = onRequest(async (request, response) => {
  logger.info("ENTER");
  try {
    const email = request.body.data.username;
    const id = todaysDocumentId();
    const doc = (await db.collection(email)
      .doc(id)
      .get()).data();

    if (doc === undefined) {
      response.send({
        data: []
      });
      return;
    }

    const registers = doc.data || [];
    const oneHour = 60 * 60 * 1000;
    const lastHourRegisters = registers
      .map((reg: any) => ({ ...reg, timestamp: new Date(Date.parse(reg.timestamp)) }))
      .filter((reg: any) => ((new Date()).getTime() - reg.timestamp.getTime()) < oneHour)

    response.send({
      data: lastHourRegisters,
    })

    logger.info("DOCUMENTS", doc);
  } catch (e) {
    logger.error(e);
    response.status(500).send("Internal Server Error");
  }
});

