"""imagician index (main) view.URLs include:/."""

from cmath import inf
import os
import pathlib
from unittest import result
import uuid
import hashlib
import flask
from flask import abort
import arrow
import imagician
from imagician.model import get_db


def get_uuid_filename():
    """Get uuid filename."""
    # Unpack flask object
    fileobj = flask.request.files["file"]
    filename = fileobj.filename

    # Compute base name (filename without directory).  We use a UUID to avoid
    # clashes with existing files, and ensure that the name is compatible with
    # the filesystem.
    stem = uuid.uuid4().hex
    suffix = pathlib.Path(filename).suffix
    uuid_basename = f"{stem}{suffix}"

    # Save to disk
    file_pth = imagician.app.config["UPLOAD_FOLDER"]/uuid_basename
    return file_pth

@imagician.app.route('/accounts/login/', methods=['POST'])
def login():
    """Use this end point to login."""
    if 'username' not in flask.request.form \
            or 'password' not in flask.request.form:
        abort(400)
    username = flask.request.form['username']
    password = flask.request.form['password']
    
    connection = get_db()
    cur = connection.execute(
        "SELECT password, is_deleted FROM users WHERE username = ?",
        (username, )
    )
    result = cur.fetchall()
    if len(result) != 1 or result[0]["is_deleted"]:
        abort(403)
    stored_password = result[0]['password']
    algorithm = stored_password.split('$')[0]
    salt = stored_password.split('$')[1]
    encrypted_password = stored_password.split('$')[2]

    hash_obj = hashlib.new(algorithm)
    password_salted = salt + password
    hash_obj.update(password_salted.encode('utf-8'))
    password_hash = hash_obj.hexdigest()

    if password_hash != encrypted_password:
        abort(403)

    flask.session['username'] = username
    
    connection.execute(
        "UPDATE users SET last_login=datetime('now') WHERE username = ?",
        (username, )
    )
    context = {"Result": "Successfully logged in!"}
    context['username'] = username
    return flask.jsonify(**context)

@imagician.app.route('/accounts/logout/', methods=['POST'])
def logout():
    """Show logout."""
    # if logged in
    if 'username' in flask.session:
        flask.session.pop('username', None)
    context = {"Result": "Successfully logged out!"}
    return flask.jsonify(**context)

@imagician.app.route('/accounts/create/', methods=['POST'])
def create_account():
    """Create a new account."""
    # If already logged in, reject
    if 'username' in flask.session:
        abort(409)

    # Parse argument
    if 'username' not in flask.request.form \
        or 'password' not in flask.request.form \
        or 'fullname' not in flask.request.form \
        or 'email' not in flask.request.form \
            or 'phone_number' not in flask.request.form:
        abort(400)
    username = flask.request.form['username']
    password = flask.request.form['password']
    fullname = flask.request.form['fullname']
    email = flask.request.form['email']
    phone = flask.request.form['phone_number']
    
    # Sanity check: make sure username not exist
    connection = get_db()
    cur = connection.execute(
        "SELECT * FROM users WHERE username = ?",
        (username, )
    )
    result = cur.fetchall()
    if len(result) != 0:
        abort(409)
    
    new_password = encrypt_password(password)
    connection.execute(
        "INSERT INTO users"
        "(username, fullname, email, phone_number, password, is_deleted) "
        "VALUES (?,?,?,?,?,0)",
        (username, fullname, email, phone, new_password)
    )

    # initialize context
    context = {"Result": "Account successfully registered and logged in!"}
    context["username"] = username
    context["fullname"] = fullname
    context["email"] = email
    context["phone"] = phone
    flask.session['username'] = username
    return flask.jsonify(**context)

@imagician.app.route('/accounts/delete/', methods=['POST'])
def delete_account():
    """Delete an account."""
    # If not logged in, reject
    if 'username' not in flask.session:
        abort(403)
    logname = flask.session['username']
    
    # Sanity check: make sure username exists
    connection = get_db()
    cur = connection.execute(
        "SELECT * FROM users WHERE username = ?",
        (logname, )
    )
    result = cur.fetchall()
    if len(result) != 1 or result[0]["is_deleted"]:
        abort(403)
    connection.execute(
        "UPDATE users SET is_deleted = 1 "
        "WHERE username = ?",
        (logname, )
    ) 
    context = {"Result": "Account successfully deleted!"}
    context["Original_username"] = logname
    flask.session.pop('username', None)
    return flask.jsonify(**context)

@imagician.app.route('/accounts/edit/', methods=['POST'])
def edit_account():
    """Edit account."""
    # If not logged in, reject
    if 'username' not in flask.session:
        abort(403)
    logname = flask.session['username']

    # Connect to database
    connection = imagician.model.get_db()
    
    # Find user info
    cur = connection.execute(
        "SELECT DISTINCT * "
        "FROM users "
        "WHERE username = ?",
        (logname, )
    )
    user_info = cur.fetchall()
    if len(user_info) != 1 or user_info[0]["is_deleted"]:
        abort(403)
        
    user_info = user_info[0]
    if "fullname" not in flask.request.form:
        fullname = user_info['fullname']
    else:
        fullname = flask.request.form['fullname']
    
    if "email" not in flask.request.form:
        email = user_info['email']
    else:
        email = flask.request.form['email']
    
    if "phone_number" not in flask.request.form:
        phone_number = user_info['phone_number']
    else:
        phone_number = flask.request.form['phone_number']
    # Parse argument
    
    connection.execute(
        "UPDATE users SET fullname = ?, email = ?, phone_number = ? "
        "WHERE username = ?",
        (fullname, email, phone_number, logname)
    ) 
    
    context = {"Result": "Account info successfully updated!"}
    context["username"] = logname
    context["fullname"] = fullname
    context["email"] = email
    context["phone"] = phone_number
    return flask.jsonify(**context)

@imagician.app.route('/accounts/info/', methods=['GET'])
def get_account_info():
    """Get info of my account."""
    # If not logged in, reject
    if 'username' not in flask.session:
        abort(403)
    logname = flask.session['username']

    # Connect to database
    connection = imagician.model.get_db()
    
    # Find user info
    cur = connection.execute(
        "SELECT DISTINCT * "
        "FROM users "
        "WHERE username = ?",
        (logname, )
    )
    user_info = cur.fetchall()
    if len(user_info) != 1 or user_info[0]["is_deleted"]:
        abort(403)
        
    context = user_info[0]
    context["password"] = "[HIDDEN]"
    return flask.jsonify(**context)

# TODO: Change this prototype: name will be the uuid of that folder name
@imagician.app.route("/uploads/<path:name>")
def download_file(name):
    """Download file."""
    if 'username' not in flask.session:
        abort(403)

    return flask.send_from_directory(
        imagician.app.config['UPLOAD_FOLDER'], name, as_attachment=True
    )


# TODO:
@imagician.app.route("/images/post_tag/", methods=['POST'])
def post_tag():
    """_summary_
        Required in flask.request.form:
            'tag': tag of the image, integer
            'imgname': name of the image, string
            'checksum': sha256 of the image, string
            'fullname_public': whether the fullname is public,
            'email_public': whether the fullname is public,
            'phone_public': whether the phone number is public,
            'time_public': whether the time is public,
            'message': the enclosed message,
            'message_encrypted': whether the message is encrypted,
        Optional in flask.request.files:
            'file': the enclosed file, if there is one
    Returns:
        403 if the user not logged in
        409 if the tag conflicts
    """
    pass

@imagician.app.route("/images/get_tag/<int:tag>/", methods=['GET'])
def get_tag(tag):
    """_summary_

    Args:
        tag (_type_): _description_
    Returns:
        404 if the tag doesn't exists
        else:
            A json of
                'id': the id of the image, integer
                'imgname': the name of the image, string
                'owner': the username of the creator of the image, string
                'checksum': the checksum of the image, string
                'fullname': if not public, then empty string
                'email': if not public, then empty string
                'phone': if not public, then empty string
                'time': if not public, then empty string
                'message': the message
                'message_encrypted': whether the message is encrypted
                'file': the folder name where the enclosed file is stored. empty if no enclosed file. 
                'authorized': if the user is logged in, and the user has been authorized
    """
    # Connect to database
    connection = imagician.model.get_db()
    
    # Find user info
    cur = connection.execute(
        "SELECT DISTINCT id "
        "FROM images "
        "WHERE tag = ?",
        (tag, )
    )
    result = cur.fetchall()
    if len(result) > 0:
        return get_id(result[0]['id'])


@imagician.app.route("/images/get_id/<int:id>/", methods=['GET'])
def get_id(id):
    """_summary_

    Args:
        id (_type_): _description_
    Returns:
        404 if the id doesn't exists
        else:
            A json of
                'id': the id of the image, integer
                'imgname': the name of the image, string
                'owner': the username of the creator of the image, string
                'checksum': the checksum of the image, string
                'fullname': if not public, then empty string
                'email': if not public, then empty string
                'phone': if not public, then empty string
                'time': if not public, then empty string
                'message': the message
                'message_encrypted': whether the message is encrypted
                'file': the folder name where the enclosed file is stored. empty if no enclosed file. 
                'authorized': if the user is logged in, and the user has been authorized
    """
    result = get_img_by_id_helper(id)
    
    # Check authorization
    result['authorized'] = False
    if 'username' in flask.session:
        username = flask.session['username']
        # Connect to database
        connection = imagician.model.get_db()
        
        # Find user info
        cur = connection.execute(
            "SELECT DISTINCT * "
            "FROM authorization "
            "WHERE imgid = ? AND username = ? AND is_deleted != 1",
            (id, username, )
        )
        authorizations = cur.fetchall()
        for authorization in authorizations:
            if authorization['status'] == 'AUTHORIZED':
                result['authorized'] = True
                break
            
    
    return flask.jsonify(**result)

def get_img_by_id_helper(id):
    """Helper for getting all the information of an image with id
                'id': the id of the image, integer
                'imgname': the name of the image, string
                'owner': the username of the creator of the image, string
                'checksum': the checksum of the image, string
                'fullname': if not public, then empty string
                'email': if not public, then empty string
                'phone': if not public, then empty string
                'time': if not public, then empty string
                'message': the message
                'message_encrypted': whether the message is encrypted
                'file': the folder name where the enclosed file is stored. empty if no enclosed file. 
    """
    # Connect to database
    connection = imagician.model.get_db()
    
    # Find user info
    cur = connection.execute(
        "SELECT DISTINCT * "
        "FROM images "
        "WHERE id = ?",
        (id, )
    )
    result = cur.fetchall()
    if len(result) != 1 or result[0]["is_deleted"]:
        abort(404)
    img_info = result[0]
    result = {}
    username = img_info['owner']
    cur = connection.execute(
        "SELECT DISTINCT * "
        "FROM users "
        "WHERE username = ?",
        (username, )
    )
    user_info = cur[0]
    result['id'] = img_info['id']
    result['imgname'] = img_info['imgname']
    result['owner'] = img_info['owner']
    result['checksum'] = img_info['checksum']
    if img_info['fullname_public']:
        result['fullname'] = user_info['fullname']
    else:
        result['fullname'] =''
    if img_info['email_public']:
        result['email'] = user_info['email']
    else:
        result['email'] =''
    if img_info['phone_public']:
        result['phone'] = user_info['phone_number']
    else:
        result['phone'] =''
    if img_info['time_public']:
        result['time'] = img_info['created']
    else:
        result['time'] =''
    result['message'] = img_info['message']
    result['message_encrypted'] = img_info['message_encrypted']
    result['file'] = img_info['file_path']
    return result

@imagician.app.route("/images/my_creation/", methods=['GET'])
def get_all_creations():
    """_summary_
    Returns:
        403 if not logged in
        else an array of json of:
            'image': a json of 
                'id': the id of the image, integer
                'imgname': the name of the image, string
                'owner': the username of the creator of the image, string
                'checksum': the checksum of the image, string
                'fullname': if not public, then empty string
                'email': if not public, then empty string
                'phone': if not public, then empty string
                'time': if not public, then empty string
                'message': the message
                'message_encrypted': whether the message is encrypted
                'file': the folder name where the enclosed file is stored. empty if no enclosed file. 
            'requests': an array of json of
                'id': the id of the request
                'imgid': the id of the image
                'username': the requester
                'message': the request message
                'status": 'PENDING', 'AUTHORIZED' or 'REJECTED'
                'created': the time of this request
    """
    # If not logged in, reject
    if 'username' not in flask.session:
        abort(403)
    username = flask.session['username']
    connection = imagician.model.get_db()
    
    # Find user info
    cur = connection.execute(
        "SELECT DISTINCT id "
        "FROM images "
        "WHERE owner = ? "
        "ORDER BY id DESC",
        (username, )
    )
    images = cur.fetchall()
    result = []
    for img in images:
        imgid = img['id']
        info = {}
        info['image'] = get_img_by_id_helper(imgid)
        cur = connection.execute(
            "SELECT DISTINCT * "
            "FROM authorization "
            "WHERE imgid = ? AND is_deleted != 1 "
            "ORDER BY id DESC",
            (imgid, )
        )
        info['requests'] = cur.fetchall()
        result.append(info)
    return flask.jsonify(**result)

@imagician.app.route("/images/my_creation/<int:imgid>/", methods=['GET'])
def get_one_creation(imgid):
    """_summary_
    Returns:
        403 if not logged in or if img with imgid doesn't belong to the user
        404 if imgid doesn't exist
        else a json of:
            'image': a json of 
                'id': the id of the image, integer
                'imgname': the name of the image, string
                'owner': the username of the creator of the image, string
                'checksum': the checksum of the image, string
                'fullname': if not public, then empty string
                'email': if not public, then empty string
                'phone': if not public, then empty string
                'time': if not public, then empty string
                'message': the message
                'message_encrypted': whether the message is encrypted
                'file': the folder name where the enclosed file is stored. empty if no enclosed file. 
            'requests': an array of json of
                'id': the id of the request
                'imgid': the id of the image
                'username': the requester
                'message': the request message
                'status": 'PENDING', 'AUTHORIZED' or 'REJECTED'
                'created': the time of this request
    """
    # If not logged in, reject
    if 'username' not in flask.session:
        abort(403)
    username = flask.session['username']
    connection = imagician.model.get_db()
    
    cur = connection.execute(
        "SELECT DISTINCT id "
        "FROM images "
        "WHERE owner = ? AND id = ?"
        "ORDER BY id DESC",
        (username, imgid, )
    )

    images = cur.fetchall()
    if len(images) != 1:
        abort(404)
    result = {}
    result['image'] = get_img_by_id_helper(imgid)
    cur = connection.execute(
        "SELECT DISTINCT * "
        "FROM authorization "
        "WHERE imgid = ? AND is_deleted != 1 "
        "ORDER BY id DESC",
        (imgid, )
    )
    result['requests'] = cur.fetchall()
    return flask.jsonify(**result)

@imagician.app.route("/requests/received_request/<int:reqid>/", methods=['GET'])
def get_one_received_request(reqid):
    """_summary_
    Returns:
        403 if not logged in or if reqid is not sent to the user
        404 if reqid doesn't exist
        else a json of:
            'image': a json of 
                'id': the id of the image, integer
                'imgname': the name of the image, string
                'owner': the username of the creator of the image, string
                'checksum': the checksum of the image, string
                'fullname': if not public, then empty string
                'email': if not public, then empty string
                'phone': if not public, then empty string
                'time': if not public, then empty string
                'message': the message
                'message_encrypted': whether the message is encrypted
                'file': the folder name where the enclosed file is stored. empty if no enclosed file. 
            'request': a json of
                'id': the id of the request
                'imgid': the id of the image
                'username': the requester
                'message': the request message
                'status": 'PENDING', 'AUTHORIZED' or 'REJECTED'
                'created': the time of this request
    """
    # If not logged in, reject
    if 'username' not in flask.session:
        abort(403)
    username = flask.session['username']
    
    connection = imagician.model.get_db()

    cur = connection.execute(
        "SELECT DISTINCT a.id, a.imgid, a.username, a.message, a.status, a. created "
        "FROM authorization a, images m "
        "WHERE a.id = ? AND m.owner = ? AND a.imgid = m.id"
        "ORDER BY id DESC",
        (reqid, username )
    )
    requests = cur.fetchall()
    if len(requests) != 0:
        abort(404)
    request = requests[0]
    result = {}
    result['image'] = get_img_by_id_helper(request['imgid'])
    result['request'] = request
    return flask.jsonify(**result)

#TODO: 
@imagician.app.route("/requests/received_request/", methods=['POST'])
def process_one_received_request():
    """_summary_
        Required in flask.request.form:
            'reqid': the id for the request
            'action': 'AUTHORIZED' or 'REJECTED'
    Returns:
        403 if the user not logged in or request doesn't belong to user
        404 if the request id doesn't exist
    """
    pass

@imagician.app.route("/requests/sent_request/", methods=['GET'])
def get_all_sent_request():
    """_summary_
    Returns:
        403 if not logged in
        else an array of json of:
            'image': a json of 
                'id': the id of the image, integer
                'imgname': the name of the image, string
                'owner': the username of the creator of the image, string
                'checksum': the checksum of the image, string
                'fullname': if not public, then empty string
                'email': if not public, then empty string
                'phone': if not public, then empty string
                'time': if not public, then empty string
                'message': the message
                'message_encrypted': whether the message is encrypted
                'file': the folder name where the enclosed file is stored. empty if no enclosed file. 
            'request': a json of
                'id': the id of the request
                'imgid': the id of the image
                'username': the requester
                'message': the request message
                'status": 'PENDING', 'AUTHORIZED' or 'REJECTED'
                'created': the time of this request
    """
    # If not logged in, reject
    if 'username' not in flask.session:
        abort(403)
    username = flask.session['username']
    
    connection = imagician.model.get_db()
    cur = connection.execute(
        "SELECT DISTINCT * "
        "FROM authorization "
        "WHERE username = ? AND is_deleted != 1 "
        "ORDER BY id DESC",
        (username, )
    )
    requests = cur.fetchall()
    result = []
    for request in requests:
        info = {}
        info['image'] = get_img_by_id_helper(request['imgid'])
        info['request'] = request
        result.append(info)
    return flask.jsonify(**result)

@imagician.app.route("/requests/sent_request/<int:reqid>/", methods=['GET'])
def get_one_sent_request(reqid):
    """_summary_
    Returns:
        403 if not logged in, or reqid doesn't belong to the user
        404 if the reqid doesn't exists
        else a json of:
            'image': a json of 
                'id': the id of the image, integer
                'imgname': the name of the image, string
                'owner': the username of the creator of the image, string
                'checksum': the checksum of the image, string
                'fullname': if not public, then empty string
                'email': if not public, then empty string
                'phone': if not public, then empty string
                'time': if not public, then empty string
                'message': the message
                'message_encrypted': whether the message is encrypted
                'file': the folder name where the enclosed file is stored. empty if no enclosed file. 
            'request': a json of
                'id': the id of the request
                'imgid': the id of the image
                'username': the requester
                'message': the request message
                'status": 'PENDING', 'AUTHORIZED' or 'REJECTED'
                'created': the time of this request
    """
    # If not logged in, reject
    if 'username' not in flask.session:
        abort(403)
    username = flask.session['username']
    
    connection = imagician.model.get_db()
    cur = connection.execute(
        "SELECT DISTINCT * "
        "FROM authorization "
        "WHERE username = ? AND id = ? AND is_deleted != 1 "
        "ORDER BY id DESC",
        (username, reqid, )
    )
    requests = cur.fetchall()
    if len(requests) != 1:
        abort(404)
    request = requests[0]
    info = {}
    info['image'] = get_img_by_id_helper(request['imgid'])
    info['request'] = request
        
    return flask.jsonify(**info)

#TODO:
@imagician.app.route("/requests/sent_request/", methods=['POST'])
def post_request():
    """_summary_
        Required in flask.request.form:
            'imgid': the id of the image
            'message': the message
    Returns:
        403 if the user not logged in
        404 if the img id doesn't exist
    """
    pass


def encrypt_password(orig_pswd):
    """Encrypt password."""
    algorithm = 'sha512'
    salt = uuid.uuid4().hex
    hash_obj = hashlib.new(algorithm)
    password_salted = salt + orig_pswd
    hash_obj.update(password_salted.encode('utf-8'))
    password_hash = hash_obj.hexdigest()
    password_db_string = "$".join([algorithm, salt, password_hash])
    # print(password_db_string)
    return password_db_string

