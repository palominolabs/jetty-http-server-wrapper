openssl genrsa -out selfsigned-key.pem 4096
openssl req -new -key selfsigned-key.pem -out selfsigned-csr.pem
openssl x509 -req -days 36500 -in selfsigned-csr.pem -signkey selfsigned-key.pem -out selfsigned-cert.pem

 openssl pkcs12 -export -out cert-and-key.p12 -in selfsigned-cert.pem -inkey selfsigned-key.pem

pkcs12 password is 'password'
