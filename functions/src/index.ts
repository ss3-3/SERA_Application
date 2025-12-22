/**
 * Cloud Functions for SERA Application
 * Handles organizer approval email notifications
 */

import {onDocumentUpdated} from "firebase-functions/v2/firestore";
import * as logger from "firebase-functions/logger";
import * as https from "https";

// Replace with your Resend API key from https://resend.com/api-keys
const RESEND_API_KEY = "re_6nzGu7vb_AESwGZqvaNy3ECaMiUMxEytU";

/**
 * Triggered when a user document is updated in Firestore
 * Sends email if organizer is approved
 */
export const onOrganizerApproved = onDocumentUpdated(
  "users/{userId}",
  async (event) => {
    const before = event.data?.before.data();
    const after = event.data?.after.data();

    if (!before || !after) {
      logger.warn("Missing before/after data");
      return;
    }

    // Check if organizer was just approved
    const wasPending = before.approvalStatus === "PENDING";
    const isApproved = after.approvalStatus === "APPROVED";
    const isOrganizer = after.role === "ORGANIZER";

    if (wasPending && isApproved && isOrganizer) {
      const email = after.email;
      const fullName = after.fullName;

      logger.info(`Organizer approved: ${email}`);

      try {
        await sendApprovalEmail(email, fullName);
        logger.info(`Approval email sent successfully to ${email}`);
      } catch (error) {
        logger.error(`Failed to send approval email to ${email}:`, error);
        throw error;
      }
    }
  }
);

/**
 * Send approval email using Resend API
 * @param {string} email - Recipient email address
 * @param {string} fullName - Recipient full name
 * @return {Promise<void>} Promise that resolves when email is sent
 */
async function sendApprovalEmail(
  email: string,
  fullName: string
): Promise<void> {
  const emailData = {
    from: "SERA App <onboarding@resend.dev>",
    to: email,
    subject: "Organizer Account Approved",
    html: `
      <h2>Your Organizer Account Has Been Approved!</h2>
      <p>Hello ${fullName},</p>
      <p>Great news! Your organizer account has been approved by an
      administrator.</p>
      <p>You can now log in to the SERA Application and start creating
      events.</p>
      <p>Thank you for your patience.</p>
      <br>
      <p>Best regards,<br>SERA Team</p>
    `,
  };

  return new Promise((resolve, reject) => {
    const data = JSON.stringify(emailData);
    const options = {
      hostname: "api.resend.com",
      path: "/emails",
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${RESEND_API_KEY}`,
        "Content-Length": data.length,
      },
    };

    const req = https.request(options, (res) => {
      let responseData = "";
      res.on("data", (chunk) => {
        responseData += chunk;
      });
      res.on("end", () => {
        if (res.statusCode === 200 || res.statusCode === 201) {
          resolve();
        } else {
          const errorMsg =
            `Email API error: ${res.statusCode} - ${responseData}`;
          reject(new Error(errorMsg));
        }
      });
    });

    req.on("error", (error) => {
      reject(error);
    });

    req.write(data);
    req.end();
  });
}
