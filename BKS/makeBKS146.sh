export CLASSPATH=bcprov-jdk15on-146.jar

keytool -importcert -v -trustcacerts -file "mycert.cer" -alias IntermediateCA -keystore "myKeystore.bks" -provider org.bouncycastle.jce.provider.BouncyCastleProvider -providerpath "bcprov-jdk15on-146.jar" -storetype BKS -storepass mysecret

keytool -list -keystore "myKeystore.bks" -provider org.bouncycastle.jce.provider.BouncyCastleProvider  -providerpath "bcprov-jdk15on-146.jar" -storetype BKS -storepass mysecret


