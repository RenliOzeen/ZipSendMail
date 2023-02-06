package org.example;

import jakarta.activation.DataHandler;
import jakarta.activation.DataSource;
import jakarta.activation.FileDataSource;
import jakarta.mail.*;
import jakarta.mail.internet.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Main {
    public static void main(String[] args) throws IOException {
        String dir = "path to dir - ZipProjectDirectory";
        File dirToZip = new File(dir);

        FileOutputStream fileOutputStream = new FileOutputStream("compressedDirectory.zip");
        ZipOutputStream zipOutputStream = new ZipOutputStream(fileOutputStream);

        zipDir(dirToZip, dirToZip.getName(), zipOutputStream);
        zipOutputStream.close();
        fileOutputStream.close();

        String fileName = "compressedDirectory.zip";
        sendMail(fileName, "emailToSend@something.domain");
    }

    private static void zipDir(File dirToZip, String dirName, ZipOutputStream zos) throws IOException {
        if (dirToZip.isHidden()) {
            return;
        }

        if (dirToZip.isDirectory()) {
            if (dirName.endsWith("/")) {
                zos.putNextEntry(new ZipEntry(dirName));
                zos.closeEntry();
            } else {
                zos.putNextEntry(new ZipEntry(dirName + "/"));
                zos.closeEntry();
            }
            File[] parts = dirToZip.listFiles();
            for (File part : parts) {
                if (!part.getName().equals("compressedDirectory.zip")) {
                    zipDir(part, dirName + "/" + part.getName(), zos);
                }
            }
            return;
        }
        FileInputStream fileInputStream = new FileInputStream(dirToZip);
        ZipEntry zipEntry = new ZipEntry(dirName);
        zos.putNextEntry(zipEntry);
        byte[] bytes = new byte[1024];
        int length;
        while ((length = fileInputStream.read(bytes)) >= 0) {
            zos.write(bytes, 0, length);
        }
        fileInputStream.close();
    }

    private static void sendMail(String filename, String emailTo) {

        String from = "emailFrom@yandex.ru";

        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.yandex.ru");

        props.put("mail.smtp.socketFactory.port", "465");
        props.put("mail.smtp.socketFactory.class",
                "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", "465");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication("username", "password");
            }
        });

        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(emailTo));
            message.setSubject("Архив проекта");

            BodyPart messageBodyPart = new MimeBodyPart();
            messageBodyPart.setText("Направляю заархивированную директорию проекта!");
            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(messageBodyPart);

            messageBodyPart = new MimeBodyPart();
            DataSource source = new FileDataSource(filename);
            messageBodyPart.setDataHandler(new DataHandler(source));
            messageBodyPart.setFileName(filename);
            multipart.addBodyPart(messageBodyPart);


            message.setContent(multipart);

            Transport.send(message);
            System.out.println("Сообщение успешно отправлено");
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
}