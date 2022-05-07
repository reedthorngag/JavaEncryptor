# JavaEncryptor
 file and folder encryptor made in java with a javafx UI
 
 uses AES256 CBC encryption
 
 # This is not secure enough to be used for any sensitive data, idrk how secure it actually is, but probably not very secure as it uses a hardcoded iv and password salt, it may even be so weak its only barely better than plaintext against someone who knows what they are doing.

# features that release v1.0 will include:
   - cmd support
   - cmd only version (I am not responsible for any ransomeware this may be used for, and this is not optimized for ransomeware, so you would be better off making your          own anyway), this will be like 200kb instead of 9mb lol
   - unique cryptographically secure iv and salt for each file (uses hardcoded values currently, definitly dont use this program for top secret information lol)
   - option to decrypt the file/folder for a set amount of time, which would start a background process that will re-encrypt the file/folder after a set period of time
   - hopefully I will manage to get proguard working, and cut the size of this down a lot.



