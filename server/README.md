Backend server for IMAGician
install: scripts/IMAGician_install
initiate database: scripts/IMAGician_install start
restart database: scripts/IMAGician_install reset
start: scripts/IMAGician_run
* ### Back-end Endpoint: POST /accounts/create/
This URL creates a new account for a user. The user's password will be salted and hashed before being stored in the database. After an account has been successfully created, the user will be automatically logged in.
* Receives a json form data of:
    1. `username`, string: the username of the newly created account
    2. `password`, string: the password of the new user
    3. `email`, string (optional): the email address of the new user
    4. `phone_number`, string (optional): the phone number of the new user
    5. `full_name`, string (optional): the fullname of the new user
* If the username already exists, then abort `409`
* Else, create the account, login the user by setting a session cookie, and return `200`

* ### Back-end Endpoint: POST /accounts/delete/
This URL deletes an existing account, and automatically logs out the user. The user must be logged in to perform this operation.
* If the user hasn't logged in, then abort `403`
* Else, delete the account, logout the user by removing the session cookie, and return `200`

* ### Back-end Endpoint: POST /accounts/edit/
This URL edits the information an existing account. The user must be logged in to perform this operation.
* Receives a json form data of:
    1. `password`, string(optional): the new password of the user
    2. `email`, string(optional): the new email address of the user
    3. `phone_number`, string(optional): the new phone number of the user
    4. `full_name`, string (optional): the fullname of the user
* If the user hasn't logged in, then abort `403`
* Else, update the account info and return `200`

* ### Back-end Endpoint: POST /accounts/login/
This URL logs in a user.
* Receives a json form data of:
    1. `username`, string: username
    2. `password`, string: password
* If the username doesn't exists or the salted password hash and the data stored in the database mismatches, then abort `403`
* Else, login the user to that account by setting a session cookie, and return `200`
* ### Back-end Endpoint: POST /accounts/logout/
This URL logs out a user.
* If the user hasn't logged in, nothing will happen
* Logout the user by removing the session cookie, and return `200`

* ### Back-end Endpoint: GET /accounts/info/
This URL returns the personal inforation of the logged in user.
* If the user hasn't logged in, then abort `403`
* Else, return a json of:
    1. `created`, string: the creation time of this account, sample: "2022-03-13 04:15:52"
    2. `email`, string: the email of the account
    3. `fullname`, string: the fullname of the account owner
    4. `last_login`, string: the time when this account was last logged in, sample: "2022-03-13 04:15:52"
    5. `phone_number`, string: the phone number of the user
    6. `username`, string: the username of the user

* ### Back-end Endpoint: POST /images/post_tag/
This URL receives and stores all the information of a tagged image. The user must be logged in to perform this operation.
* Receives a json form data of:
    1. `tag`, string: the tag embedded in the image, must be unique across all images.
    2. `imgname`, string: the name of the image.
    3. `checksum`, string: the SHA256 hash of the image.
    4. `username_public`, bool (optional): whether the username of the creator will be shown when others examine this image. Default false.
    5. `fullname_public`, bool (optional): whether the user's fullname will be shown when others examine this image. Default false.
    6. `email_public`, bool (optional): whether the user's email will be shown when others examine this image. Default false.
    7. `phone_public`, bool (optional): whether the user's phone will be shown when others examine this image. Default false.
    8. `time_public`, bool (optional): whether the upload time of this image will be shown when others examine this image. Default false.
    9. `message`, string: the enclosed message.
    10. `message_encrypted`, bool: true if the message is cyphertext, else false.
    11. `file`, file (optional): the enclosed file. If there is no enclosed file, there shouldn't be this field. Max size 16 MB, allowed extensions are 'png', 'jpg', 'jpeg', 'gif', 'pdf', 'zip'.
* If the user hasn't logged in, then abort `403`.
* If another image is already using this tag, then abort `409`.
* If the field `file` exists in the request form, then store the file in a correct folder.
* Store all the info and return `200`.

* ### Back-end Endpoint: GET /images/get_tag/\<string:tag\>/
This URL returns the public information of an image that has tag embedded.
* Receives a tag from the url `tag` field (NOT from a json form)
* If no image has the tag, then abort `404`
* Else, return a json of:
    1. `authorized`, bool: true when the user is logged in, and the user has been authorized by the creator or the user is the creator, else false.
    2. `checksum`, string: the checksum of the image.
    3. `email`, string: the email of the creator. Empty string if the creator didn't disclose this info.
    4. `file`, string: the name of the enclosed file. Empty string if no file was enclosed.
    5. `folder`, string: the folder where the enclosed file is stored in the server. Will be used in `/uploads/<string:uuid>/` endpoint. Empty string if no file was enclosed.
    6. `fullname`, string: the fullname of the creator. Empty string if the creator didn't disclose this info.
    7. `imgname`, string: the name of the image.
    8. `message`, string: the enclosed message.
    9. `message_encrypted`, bool: true if the message is cyphertext, else false.
    10. `owner`, string: the username of the creator.
    11. `phone`, string: the phone number of the creator. Empty string if the creator didn't disclose this info.
    12. `tag`, string: the tag of the image.
    13. `time`, string: the time when the info of image was stored, sample: "2022-03-13 04:15:52". Empty string if the creator didn't disclose this info.

* ### Back-end Endpoint: GET /uploads/\<string:folder\>/
This URL returns the file stored in the folder. From the endpoint `/images/get_tag/<string:tag>/` and `/images/get_tag/<int:id>/`, the user will receive the name of the enclosed file and the folder where that file is stored. The file will be the only file in that folder. This end point will return the file stored in that folder. 
* Receives a folder name from the url `folder` field (the folder name only, don't include the file name)
* Log in not required.
* If the folder doesn't exist, abort `404`.
* Else, return the file stored in that folder. 

* ### Back-end Endpoint: GET /images/my_creation/
This URL returns all the information (including the request information) of all the images created by the user. The user must be logged in to perform this operation.
* If the user hasn't logged in, then abort `403`
* Return a json that has only one element called `result` in it. The value of the result is an array of json. Each element of that array is in the form of:
    1. `image`, json of the form:
        1. `checksum`, string: the checksum of the image.
        2. `email`, string: the email of the creator. Empty string if the creator didn't disclose this info.
        3. `file`, string: the name of the enclosed file. Empty string if no file was enclosed.
        4. `folder`, string: the folder where the enclosed file is stored in the server. Will be used in `/uploads/<string:uuid>/` endpoint. Empty string if no file was enclosed.
        5. `fullname`, string: the fullname of the creator. Empty string if the creator didn't disclose this info.
        6. `imgname`, string: the name of the image.
        7. `message`, string: the enclosed message.
        8. `message_encrypted`, bool: true if the message is cyphertext, else false.
        9. `num_pending`, integer: the number of pending request for this image.
        10. `owner`, string: the username of the creator.
        11. `phone`, string: the phone number of the creator. Empty string if the creator didn't disclose this info.
        12. `tag`, string: the tag of the image.
        13. `time`, string: the time when the info of image was stored, sample: "2022-03-13 04:15:52". Empty string if the creator didn't disclose this info.
    2. `requests`, an array of json. Each element of that array is in the form:
        1. `created`: the time when this request was created, sample: "2022-03-13 04:15:52".
        2. `id`: the request id of this request.
        3. `imgtag`: the tag of the image that this request was sent to.
        4. `message`: the request message.
        5. `status`: status of the request: "REJECTED", "GRANTED" or "PENDING".
        6. `username`: the username of the requester.
* The images in the array are in the order of most recently posted to least recently posted.
* The requests in the array are in the order of most recently posted to least recently posted.

* ### Back-end Endpoint: GET /images/my_creation/\<string:tag\>/
This URL returns the information (including the request information) of one images created by the user. The user must be logged in to perform this operation.
* Receives the tag of the image from the url `tag` field (NOT from a json form). 
* If the user hasn't logged in, then abort `403`
* If the img with the id doesn't exist or doesn't belong to the user, return 404
* The return value is a json. The json is of the same form as one element in the `result` field of the return value of `/images/my_creation/` endpoint

* ### Back-end Endpoint: GET /requests/received_request/\<int:reqid\>/
This URL returns the information (including the information of the image requested) of one request sent to the user. The user must be logged in to perform this operation.
* Receives the id of the request from the url `reqid` field (NOT from a json form). 
* If the user hasn't logged in, then abort `403`
* If the request with the id doesn't exist or wasn't sent to the user, abort `404`
* The return value is a json of the form
    1. `image`, json of the form:
        1. `checksum`, string: the checksum of the image.
        2. `email`, string: the email of the creator. Empty string if the creator didn't disclose this info.
        3. `file`, string: the name of the enclosed file. Empty string if no file was enclosed.
        4. `folder`, string: the folder where the enclosed file is stored in the server. Will be used in `/uploads/<string:uuid>/` endpoint. Empty string if no file was enclosed.
        5. `fullname`, string: the fullname of the creator. Empty string if the creator didn't disclose this info.
        6. `imgname`, string: the name of the image.
        7. `message`, string: the enclosed message.
        8. `message_encrypted`, bool: true if the message is cyphertext, else false.
        9. `owner`, string: the username of the creator.
        10. `phone`, string: the phone number of the creator. Empty string if the creator didn't disclose this info.
        11. `tag`, string: the tag of the image.
        12. `time`, string: the time when the info of image was stored, sample: "2022-03-13 04:15:52". Empty string if the creator didn't disclose this info.
    2. `request`, a json of the form:
        1. `created`: the time when this request was created, sample: "2022-03-13 04:15:52".
        2. `id`: the request id of this request.
        3. `imgtag`: the tag of the image that this request was sent to.
        4. `message`: the request message.
        5. `status`: status of the request: "REJECTED", "GRANTED" or "PENDING".
        6. `username`: the username of the requester.

* ### Back-end Endpoint: POST /requests/received_request/
This URL processes one received usage request. The user must be logged in to perform this operation.
* Receives a json form data of:
    1. `reqid`, integer: the id of that usage request to be processed
    2. `action`, string: "GRANTED" or "REJECTED"
* If the user hasn't logged in, then abort `403`.
* If the request with the id doesn't exist or wasn't sent to the user, abort `404`
* Else, update the request, and return `200`.


* ### Back-end Endpoint: GET /requests/sent_request/
This URL returns all the information (including the information if the requested image) of all the requests sent by the user. The user must be logged in to perform this operation.
* If the user hasn't logged in, then abort `403`
* Return a json that has only one element called `result` in it. The value of the result is an array of json. Each element of that array is in the form of:
    1. `image`, json of the form:
        1. `checksum`, string: the checksum of the image.
        2. `email`, string: the email of the creator. Empty string if the creator didn't disclose this info.
        3. `file`, string: the name of the enclosed file. Empty string if no file was enclosed.
        4. `folder`, string: the folder where the enclosed file is stored in the server. Will be used in `/uploads/<string:uuid>/` endpoint. Empty string if no file was enclosed.
        5. `fullname`, string: the fullname of the creator. Empty string if the creator didn't disclose this info.
        6. `imgname`, string: the name of the image.
        7. `message`, string: the enclosed message.
        8. `message_encrypted`, bool: true if the message is cyphertext, else false.
        9. `owner`, string: the username of the creator.
        10. `phone`, string: the phone number of the creator. Empty string if the creator didn't disclose this info.
        11. `tag`, string: the tag of the image.
        12. `time`, string: the time when the info of image was stored, sample: "2022-03-13 04:15:52". Empty string if the creator didn't disclose this info.
    2. `request`, a json of the form:
        1. `created`: the time when this request was created, sample: "2022-03-13 04:15:52".
        2. `id`: the request id of this request.
        3. `imgtag`: the tag of the image that this request was sent to.
        4. `message`: the request message.
        5. `status`: status of the request: "REJECTED", "GRANTED" or "PENDING".
        6. `username`: the username of the requester.
* The requests in the array are in the order of most recently requested to least recently requested.

* ### Back-end Endpoint: GET /requests/sent_request/\<int:reqid\>/
This URL returns the information (including the request information) of one request sent by the user. The user must be logged in to perform this operation. 
* Receives the id of the request from the url `reqid` field (NOT from a json form).
* If the user hasn't logged in, then abort `403`.
* If the req with the id doesn't exist or wasn't sent by the user, return 404.
* The return value is a json. The json is of the same form as one element in the `result` field of the return value of `/requests/sent_request/` endpoint.

* ### Back-end Endpoint: POST /requests/post_request/
This URL sends one usage request to an image.
* Receives a json form data of:
    1. `imgtag`, string: the tag of the image requested .
    2. `message`, string: the request message.
* If the user hasn't logged in, then abort `403`.
* If the imgid doesn't exist, abort `404`.
* Else, store this request information, mark the status as "PENDING", and return `200`.