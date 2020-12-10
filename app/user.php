<?php 

$app->post('/login', function ( $request,  $response, $args) {

    require_once __DIR__ . '/../bootstrap/dbconnection.php';
    
    $output = array();
    $requestData = array();

    $requestData['uid'] = $request->getParsedBody()['uid'];
    $requestData['name'] = $request->getParsedBody()['name'];
    $requestData['email'] = $request->getParsedBody()['email'];
    $requestData['profileUrl'] = $request->getParsedBody()['profileUrl'];
    $requestData['coverUrl'] = $request->getParsedBody()['coverUrl'];
    $requestData['userToken'] = $request->getParsedBody()['userToken'];

    $query = $pdo->prepare("SELECT `uid` from `user` WHERE `uid` = :uid LIMIT 1");
    $query->bindparam(':uid', $requestData['uid']);
    $query->execute();
    $errorData = $query->errorInfo();
    // if errorData is not null that means error found & no data is loaded
    if($errorData[1]){
       return checkError($response, $errorData);
    }
    $count = $query->rowCount();

    if($count==1){
        // update data because this user already exist
    $query =$pdo->prepare("UPDATE `user` SET  
    `name` = :name, 
    `email` = :email,
    `profileUrl` = :profileUrl,
    `coverUrl` = :coverUrl,
	`userToken` = :userToken
     WHERE `uid` = :uid; ");      
    $query->execute($requestData);
    $errorData = $query->errorInfo();
    } else{
        // create user
        $query = $pdo->prepare("INSERT INTO `user` 
        (`uid`, `name`, `email`, `profileUrl`, `coverUrl`,`userToken`) VALUES 
        (:uid, :name, :email, :profileUrl, :coverUrl, :userToken); ");
        $query->execute($requestData);
        $errorData = $query->errorInfo();
    }

    if($errorData[1]){
        return checkError($response, $errorData);
     }
});


$app->get('/loadprofileinfo', function($request, $response, $args) {

    require_once __DIR__ . '/../bootstrap/dbconnection.php';
	
	 /*
	  current_state 
      1 = We are friends 
      2 = we have sent friend request to that person  
      3 = We have received friend request from that person  
      4 = we are unknown 
      5 = Our own profile
   */

    $output = array();
    $userId = $request->getQueryParams()['userId'];
    $state = 0;

    if(isset($request->getQueryParams()['current_state'])) {
        $state = $request->getQueryParams()['current_state'];
    } else {

    }

    $query = $pdo->prepare('SELECT * FROM `user` WHERE `uid` = :userId');
	$query->bindParam(':userId', $userId, PDO::PARAM_STR);
	$query->execute();
    $errorData = $query->errorInfo();
    if($errorData[1]){
        return checkError($response,$errorData);
    }

    $result = $query->fetch(PDO::FETCH_ASSOC);

	$result['state'] = $state;
	$output['status']  = 200;
	$output['message'] = "Profile Data Retrieved";
	$output['profile'] = $result;

	$payload = json_encode($output);
	$response->getBody()->write($payload);
    return $response->withHeader('Content-Type', 'application/json')->withStatus(200);
    
});

$app->post('/uploadImage',function($request,  $response,  $args) {
	include __DIR__ .'/../bootstrap/dbconnection.php';

    $msg = "";
	$uid = $request->getParsedBody()['uid'];
	$isCoverImage = $request->getParsedBody()['isCoverImage'];


	if (move_uploaded_file( $_FILES ['file'] ["tmp_name"], "../uploads/" . $_FILES ["file"] ["name"] )) {
		
		if($isCoverImage=='true') {
			$query ="UPDATE  `user` SET `coverUrl` = :uploadUrl WHERE `uid` = :uid; ";
			$msg = "Cover Picture uploaded Successfully ";
		} else {
			$query = "UPDATE  `user` SET `profileurl` = :uploadUrl WHERE `uid` = :uid; ";
			$msg = "Profile Picture uploaded Successfully ";
        }

		$imageLocation = "../uploads/" . $_FILES ["file"] ["name"];
		$query = $pdo->prepare($query);
		$query->bindParam(':uid', $uid, PDO::PARAM_STR);
        $query->bindParam(':uploadUrl', $imageLocation, PDO::PARAM_STR);	 
		$query->execute();
		
		$errorData = $query->errorInfo();
		if($errorData[1]){
			return checkError($response, $errorData);
		}

		$output['status']  = 200;
		$output['message'] = $msg;
		$output['extra'] = $imageLocation;

		$payload = json_encode($output);
		$response->getBody()->write($payload);
		return $response->withHeader('Content-Type', 'application/json')->withStatus(200);

	} else{
		
		$output['status']  = 500;
		$output['message'] = "Couldn't Upload Image to Server !";

		$payload = json_encode($output);
		$response->getBody()->write($payload);
		return $response->withHeader('Content-Type', 'application/json')->withStatus(500);
    }
    
    if(isset($request->getParsedBody()['name'])) {
        $name = $request->getParsedBody()['name'];
        $query = "UPDATE  `user` SET `name` = :name WHERE `uid` = :uid; ";
        $msg = "Name Updated Successfully ";

        $query = $pdo->prepare($query);
        $query->bindParam(':name', $name, PDO::PARAM_STR);
        $query->execute();

        $errorData = $query->errorInfo();
		if($errorData[1]){
			return checkError($response, $errorData);
		}

		$output['status']  = 200;
		$output['message'] = $msg;
        $output['extra'] = $name;
        
        $payload = json_encode($output);
		$response->getBody()->write($payload);
		return $response->withHeader('Content-Type', 'application/json')->withStatus(200);
    }
	
});


?>




