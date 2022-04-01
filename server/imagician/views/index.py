"""imagician index (main) view.URLs include:/."""

from cmath import inf
import os
import pathlib
import uuid
import hashlib
import flask
from flask import abort
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

    # Parse argument
    if 'username' not in flask.request.form \
        or 'password' not in flask.request.form:
        abort(400)
    username = flask.request.form['username']
    password = flask.request.form['password']
    fullname = flask.request.form['fullname'] if 'fullname' in flask.request.form else ''
    email = flask.request.form['email'] if 'email' in flask.request.form else ''
    phone = flask.request.form['phone_number'] if 'phone_number' in flask.request.form else ''
    
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
        "SELECT DISTINCT username, fullname, email, phone_number, created, last_login "
        "FROM users "
        "WHERE username = ? AND is_deleted = 0",
        (logname, )
    )
    user_info = cur.fetchall()
    if len(user_info) != 1:
        abort(403)
        
    context = user_info[0]
    return flask.jsonify(**context)

@imagician.app.route("/uploads/<string:uuid>/")
def download_file(uuid):
    """Download file."""
    if uuid.find('/') != -1 or uuid.find('\\') != -1:
        abort(404)
    upload_dir = pathlib.Path(imagician.app.config['UPLOAD_FOLDER'], uuid)
    if not os.path.isdir(upload_dir):
        abort(404)
    if len(os.listdir(upload_dir)) > 0:
        file_path = os.path.abspath(os.listdir(upload_dir)[0])
        filename = os.path.basename(file_path)
    else:
        abort(404)
    return flask.send_from_directory(
        upload_dir, filename, as_attachment=True
    )


@imagician.app.route("/images/post_tag/", methods=['POST'])
def post_tag():
    """_summary_
        Required in flask.request.form:
            'tag': tag of the image, string
            'imgname': name of the image, string
            'checksum': sha256 of the image, string
            'username_public': whether the username is public,
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
    if 'username' not in flask.session:
        abort(403)
    username = flask.session['username']
    
    tag = flask.request.form['tag']
    imgname = flask.request.form['imgname']
    checksum = flask.request.form['checksum']
    username_public = flask.request.form['username_public'] if 'username_public' in flask.request.form else 0
    fullname_public = flask.request.form['fullname_public'] if 'fullname_public' in flask.request.form else 0
    email_public = flask.request.form['email_public'] if 'email_public' in flask.request.form else 0
    phone_public = flask.request.form['phone_public'] if 'phone_public' in flask.request.form else 0
    time_public = flask.request.form['time_public'] if 'time_public' in flask.request.form else 0
    message = flask.request.form['message']
    message_encrypted = flask.request.form['message_encrypted']

    connection = imagician.model.get_db()
    
    cur = connection.execute(
        "SELECT * FROM images WHERE tag = ?",
        (flask.request.form['tag'], )
    )

    if len(cur.fetchall()) > 0:
        abort(409)
    
    if 'file' in flask.request.files:
        filename = flask.request.files['file'].filename
        if filename.find('/') != -1 or filename.find('\\') != -1:
            abort(400)
        folder_name = uuid.uuid4().hex
        folder = imagician.app.config["UPLOAD_FOLDER"]/pathlib.Path(folder_name)
        folder.mkdir(parents=False, exist_ok=False)
        file_pth = folder/filename
        flask.request.files['file'].save(file_pth)
        
    else:
        folder_name = ""

    connection.execute(
        "INSERT INTO images(tag, imgname, owner, checksum, username_public, fullname_public, "
        "email_public, phone_public, time_public, message, message_encrypted, "
        "file_path, is_deleted) "
        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 0)",
        (tag, imgname, username, checksum, username_public, fullname_public, email_public, 
        phone_public, time_public, message, message_encrypted, folder_name, )
    )
    context = {}
    return flask.jsonify(**context)


@imagician.app.route("/images/get_tag/<string:tag>/", methods=['GET'])
def get_tag(tag):
    """_summary_

    Args:
        tag (_type_): _description_
    Returns:
        404 if the tag doesn't exists
        else:
            A json of
                'imgname': the name of the image, string
                'owner': the username of the creator of the image, string
                'checksum': the checksum of the image, string
                'fullname': if not public, then empty string
                'email': if not public, then empty string
                'phone': if not public, then empty string
                'time': if not public, then empty string
                'message': the message
                'message_encrypted': whether the message is encrypted
                'folder': the folder name where the enclosed file is stored. empty if no enclosed file. 
                'file': the name of the enclosed file. empty if no enclosed file. 
                'authorized': if the user is logged in, and the user has been authorized
                'tag': the tag of the image
    """
    result = get_img_by_tag_helper(tag, 1)
    
    # Check authorization
    result['authorized'] = 0
    if 'username' in flask.session:
        username = flask.session['username']
        # Connect to database
        if username == result['owner']:
            result['authorized'] = 1
        else:
            connection = imagician.model.get_db()
            
            # Find user info
            cur = connection.execute(
                "SELECT DISTINCT * "
                "FROM authorization "
                "WHERE imgtag = ? AND username = ? AND is_deleted != 1",
                (tag, username, )
            )
            authorizations = cur.fetchall()
            for authorization in authorizations:
                if authorization['status'] == 'GRANTED':
                    result['authorized'] = 1
                    break
    if not result['username_public']:
        result['owner'] = ''
    result.pop('username_public')
    return flask.jsonify(**result)

def get_img_by_tag_helper(tag, keep_owner = 0):
    """Helper for getting all the information of an image with id
                'imgname': the name of the image, string
                'owner': the username of the creator of the image, string
                'checksum': the checksum of the image, string
                'fullname': if not public, then empty string
                'email': if not public, then empty string
                'phone': if not public, then empty string
                'time': if not public, then empty string
                'message': the message
                'message_encrypted': whether the message is encrypted
                'folder': the folder name where the enclosed file is stored. empty if no enclosed file. 
                'file': the name of the enclosed file. empty if no enclosed file. 
                'tag': the tag of the image
    """
    # Connect to database
    connection = imagician.model.get_db()
    
    # Find user info
    cur = connection.execute(
        "SELECT DISTINCT * "
        "FROM images "
        "WHERE tag = ? AND is_deleted != 1",
        (tag, )
    )
    result = cur.fetchall()
    if len(result) != 1:
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
    user_info = cur.fetchall()[0]
    result['imgname'] = img_info['imgname']
    result['checksum'] = img_info['checksum']
    
    if keep_owner:
        result['owner'] = img_info['owner']
        result['username_public'] = img_info['username_public']
    else:
        if img_info['username_public']:
            result['owner'] = img_info['owner']
        else:
            result['owner'] = ''
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
    result['folder'] = img_info['file_path']
    result['tag'] = img_info['tag']
    result['file'] = ''
    if result['folder'] != '':
        upload_dir = pathlib.Path(imagician.app.config['UPLOAD_FOLDER'], result['folder'])
        if len(os.listdir(upload_dir)) > 0:
            file_path = os.path.abspath(os.listdir(upload_dir)[0])
            result['file']  = os.path.basename(file_path)
    return result

@imagician.app.route("/images/my_creation/", methods=['GET'])
def get_all_creations():
    """_summary_
    Returns:
        403 if not logged in
        else a json of
            'result': an array of json of:
                    'image': a json of 
                        'imgname': the name of the image, string
                        'owner': the username of the creator of the image, string
                        'checksum': the checksum of the image, string
                        'fullname': if not public, then empty string
                        'email': if not public, then empty string
                        'phone': if not public, then empty string
                        'time': if not public, then empty string
                        'message': the message
                        'message_encrypted': whether the message is encrypted
                        'folder': the folder name where the enclosed file is stored. empty if no enclosed file. 
                        'file': the name of the enclosed file. empty if no enclosed file. 
                        'tag': the tag of the image
                        'num_pending': number of pending requests
                    'requests': an array of json of
                        'id': the id of the request
                        'imgtag': the tag of the image
                        'username': the requester
                        'message': the request message
                        'status": 'PENDING', 'GRANTED' or 'REJECTED'
                        'created': the time of this request
    """
    # If not logged in, reject
    if 'username' not in flask.session:
        abort(403)
    username = flask.session['username']
    connection = imagician.model.get_db()
    
    # Find user info
    cur = connection.execute(
        "SELECT DISTINCT tag "
        "FROM images "
        "WHERE owner = ? "
        "ORDER BY created DESC",
        (username, )
    )
    images = cur.fetchall()
    result = []
    for img in images:
        tag = img['tag']
        info = {}
        info['image'] = get_img_by_tag_helper(tag)
        info['image']['num_pending'] = 0
        cur = connection.execute(
            "SELECT DISTINCT created, id, imgtag, message, status, username "
            "FROM authorization "
            "WHERE imgtag = ? AND is_deleted != 1 "
            "ORDER BY id DESC",
            (tag, )
        )
        info['requests'] = cur.fetchall()
        for request in info['requests']:
            if request['status'] == 'PENDING':
                info['image']['num_pending'] += 1
        result.append(info)
    return flask.jsonify(**{'result':result})

@imagician.app.route("/images/my_creation/<string:tag>/", methods=['GET'])
def get_one_creation(tag):
    """_summary_
    Returns:
        403 if not logged in or if img with imgid doesn't belong to the user
        404 if imgid doesn't exist
        else a json of:
            'image': a json of 
                'imgname': the name of the image, string
                'owner': the username of the creator of the image, string
                'checksum': the checksum of the image, string
                'fullname': if not public, then empty string
                'email': if not public, then empty string
                'phone': if not public, then empty string
                'time': if not public, then empty string
                'message': the message
                'message_encrypted': whether the message is encrypted
                'folder': the folder name where the enclosed file is stored. empty if no enclosed file. 
                'tag': the tag of the image
                'num_pending': number of pending requests
            'requests': an array of json of
                'id': the id of the request
                'imgtag': the id of the image
                'username': the requester
                'message': the request message
                'status": 'PENDING', 'GRANTED' or 'REJECTED'
                'created': the time of this request
    """
    # If not logged in, reject
    if 'username' not in flask.session:
        abort(403)
    username = flask.session['username']
    connection = imagician.model.get_db()
    
    cur = connection.execute(
        "SELECT DISTINCT tag "
        "FROM images "
        "WHERE owner = ? AND tag = ? AND is_deleted != 1",
        (username, tag, )
    )

    images = cur.fetchall()
    if len(images) != 1:
        abort(404)
    result = {}
    result['image'] = get_img_by_tag_helper(tag)
    result['image']['num_pending'] = 0
    cur = connection.execute(
        "SELECT DISTINCT created, id, imgtag, message, status, username "
        "FROM authorization "
        "WHERE imgtag = ? AND is_deleted != 1 "
        "ORDER BY id DESC",
        (tag, )
    )
    result['requests'] = cur.fetchall()
    for request in result['requests']:
            if request['status'] == 'PENDING':
                result['image']['num_pending'] += 1
    return flask.jsonify(**result)

@imagician.app.route("/requests/received_request/<int:reqid>/", methods=['GET'])
def get_one_received_request(reqid):
    """_summary_
    Returns:
        403 if not logged in or if reqid is not sent to the user
        404 if reqid doesn't exist
        else a json of:
            'image': a json of 
                'imgname': the name of the image, string
                'owner': the username of the creator of the image, string
                'checksum': the checksum of the image, string
                'fullname': if not public, then empty string
                'email': if not public, then empty string
                'phone': if not public, then empty string
                'time': if not public, then empty string
                'message': the message
                'message_encrypted': whether the message is encrypted
                'folder': the folder name where the enclosed file is stored. empty if no enclosed file. 
                'file': the name of the enclosed file. empty if no enclosed file. 
                'tag': the tag of the image
            'request': a json of
                'id': the id of the request
                'imgtag': the tag of the image
                'username': the requester
                'message': the request message
                'status": 'PENDING', 'GRANTED' or 'REJECTED'
                'created': the time of this request
    """
    # If not logged in, reject
    if 'username' not in flask.session:
        abort(403)
    username = flask.session['username']
    
    connection = imagician.model.get_db()

    cur = connection.execute(
        "SELECT DISTINCT a.id, a.imgtag, a.username, a.message, a.status, a.created "
        "FROM authorization a, images m "
        "WHERE a.id = ? AND m.owner = ? AND a.imgtag = m.tag AND a.is_deleted != 1 AND m.is_deleted !=1",
        (reqid, username, )
    )
    requests = cur.fetchall()
    if len(requests) != 1:
        abort(404)
    request = requests[0]
    result = {}
    result['image'] = get_img_by_tag_helper(request['imgtag'])
    result['request'] = request
    return flask.jsonify(**result)

@imagician.app.route("/requests/received_request/", methods=['POST'])
def process_one_received_request():
    """_summary_
        Required in flask.request.form:
            'reqid': the id for the request
            'action': 'GRANTED' or 'REJECTED'
    Returns:
        403 if the user not logged in or request doesn't belong to user
        404 if the request id doesn't exist
    """
    # If not logged in, reject
    if 'username' not in flask.session:
        abort(403)
    logname = flask.session['username']
    reqid = flask.request.form['reqid']
    action = flask.request.form['action']
    
    # Sanity check: make sure req exists
    connection = get_db()
    cur = connection.execute(
        "SELECT DISTINCT * "
        "FROM authorization a, images m "
        "WHERE a.id = ? AND m.owner = ? AND a.imgtag = m.tag AND a.is_deleted != 1 AND m.is_deleted !=1",
        (reqid, logname, )
    )
    if len(cur.fetchall()) < 1:
        abort(404)

    connection.execute(
        "UPDATE authorization SET status = ? WHERE id = ?",
        (action, reqid, )
    )

    context = {}
    return flask.jsonify(**context)

@imagician.app.route("/requests/sent_request/", methods=['GET'])
def get_all_sent_request():
    """_summary_
    Returns:
        403 if not logged in
        else a json of
            'result': an array of json of:
                'image': a json of 
                    'imgname': the name of the image, string
                    'owner': the username of the creator of the image, string
                    'checksum': the checksum of the image, string
                    'fullname': if not public, then empty string
                    'email': if not public, then empty string
                    'phone': if not public, then empty string
                    'time': if not public, then empty string
                    'message': the message
                    'message_encrypted': whether the message is encrypted
                    'folder': the folder name where the enclosed file is stored. empty if no enclosed file. 
                    'file': the name of the enclosed file. empty if no enclosed file. 
                    'tag': the tag of the image
                'request': a json of
                    'id': the id of the request
                    'imgtag': the tag of the image
                    'username': the requester
                    'message': the request message
                    'status": 'PENDING', 'GRANTED' or 'REJECTED'
                    'created': the time of this request
    """
    # If not logged in, reject
    if 'username' not in flask.session:
        abort(403)
    username = flask.session['username']
    
    connection = imagician.model.get_db()
    cur = connection.execute(
        "SELECT DISTINCT created, id, imgtag, message, status, username "
        "FROM authorization "
        "WHERE username = ? AND is_deleted != 1 "
        "ORDER BY id DESC",
        (username, )
    )
    requests = cur.fetchall()
    result = []
    for request in requests:
        info = {}
        info['image'] = get_img_by_tag_helper(request['imgtag'])
        info['request'] = request
        result.append(info)
    return flask.jsonify(**{'result':result})

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
                'folder': the folder name where the enclosed file is stored. empty if no enclosed file. 
                'file': the name of the enclosed file. empty if no enclosed file. 
            'request': a json of
                'id': the id of the request
                'imgid': the id of the image
                'username': the requester
                'message': the request message
                'status": 'PENDING', 'GRANTED' or 'REJECTED'
                'created': the time of this request
    """
    print("Here")
    # If not logged in, reject
    if 'username' not in flask.session:
        abort(403)
    username = flask.session['username']
    
    connection = imagician.model.get_db()
    cur = connection.execute(
        "SELECT DISTINCT created, id, imgtag, message, status, username "
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
    info['image'] = get_img_by_tag_helper(request['imgtag'])
    info['request'] = request
        
    return flask.jsonify(**info)

@imagician.app.route("/requests/post_request/", methods=['POST'])
def post_request():
    """_summary_
        Required in flask.request.form:
            'imgtag': the id of the image
            'message': the message
    Returns:
        403 if the user not logged in
        404 if the img id doesn't exist
    """
    # If not logged in, reject
    if 'username' not in flask.session:
        abort(403)
    logname = flask.session['username']
    imgtag = flask.request.form['imgtag']
    message = flask.request.form['message']

    get_img_by_tag_helper(imgtag)

    connection = imagician.model.get_db()
    connection.execute(
        "INSERT INTO authorization(imgtag, username, message, status, is_deleted) "
        "VALUES (?, ?, ?, ?, 0)",
        (imgtag, logname, message, "PENDING", )
    )

    context = {}
    return flask.jsonify(**context)

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

