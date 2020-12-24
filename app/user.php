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

    $query = $pdo->prepare("SELECT `uid` from `users` WHERE `uid` = :uid LIMIT 1");
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
    $query =$pdo->prepare("UPDATE `users` SET  
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
        $query = $pdo->prepare("INSERT INTO `users` 
        (`uid`, `name`, `email`, `profileUrl`, `coverUrl`,`userToken`) VALUES 
        (:uid, :name, :email, :profileUrl, :coverUrl, :userToken); ");
        $query->execute($requestData);
        $errorData = $query->errorInfo();
    }

    if($errorData[1]){
        return checkError($response, $errorData);
     }

     $output['status'] = 200;
     $output['message'] = "Login Sucess";
     $output['auth'] = $requestData;
 
     $payload = json_encode($output);
     $response->getBody()->write($payload);
 
     return $response->withHeader('Content-Type','application/json')->withStatus(200);
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
		$profileId = $request->getQueryParams()['profileId'];
		
        $request = checkRequest($userId,$profileId);
        if($request) {
			if($request['sender']==$userId){
				// we have send the request
				$state = "2";
			}else{
				$state="3";
				//we have received the request
			}
		} else{
			if(checkFriend($userId,$profileId)){
				$state = "1";
				//we are friends
			}else{
				$state="4";
				//we are unknown to one another
			}
		}
		$userId = $profileId;
    }

    $query = $pdo->prepare('SELECT * FROM `users` WHERE `uid` = :userId');
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
	
    if(isset($_FILES['file']['tmp_name']) && isset($request->getParsedBody()['isCoverImage'])) {

        if (move_uploaded_file( $_FILES ['file'] ["tmp_name"], "../uploads/" . $_FILES ["file"] ["name"] )) {
        
            $isCoverImage = $request->getParsedBody()['isCoverImage'];

            if($isCoverImage=='true') {
                $query ="UPDATE  `users` SET `coverUrl` = :uploadUrl WHERE `uid` = :uid; ";
                $msg = "Cover Picture uploaded Successfully ";
            } else {
                $query = "UPDATE  `users` SET `profileurl` = :uploadUrl WHERE `uid` = :uid; ";
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
    
        } else {
            $output['status']  = 500;
            $output['message'] = "Couldn't Upload Image to Server !";
    
            $payload = json_encode($output);
            $response->getBody()->write($payload);
            return $response->withHeader('Content-Type', 'application/json')->withStatus(500);
        }

    }

    
    if(isset($request->getParsedBody()['name'])) {
        $name = $request->getParsedBody()['name'];
        $query = "UPDATE  `users` SET `name` = :name WHERE `uid` = :uid; ";
        $msg = "Name Updated Successfully ";

        $query = $pdo->prepare($query);
        $query->bindParam(':uid', $uid, PDO::PARAM_STR);
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

// Api for search
$app->get('/search',function($request,  $response,  $args){
    include __DIR__ .'/../bootstrap/dbconnection.php';
    
	$keyword = $request->getQueryParams()['keyword'];

	$query = $pdo->prepare("SELECT * from users where name like '%$keyword%' limit 10");
	$query->execute();
	$errorData = $query->errorInfo();
	if($errorData[1]){
		return checkError($response, $errorData);
	}

	$results = $query->fetchAll(PDO::FETCH_ASSOC);

	$output['status']  = 200;
	$output['message'] = "Search";
	$output['user'] = $results;

	$payload = json_encode($output);
	$response->getBody()->write($payload);
	return $response->withHeader('Content-Type', 'application/json')->withStatus(200);

});

//Api for personalized timeline
$app->get('/getnewsfeed',function($request,  $response,  $args){
	include __DIR__ . '/../bootstrap/dbconnection.php';
  
   $uid = $request->getQueryParams()['uid'];
   $limit = $request->getQueryParams()['limit'];
   $offset = $request->getQueryParams()['offset'];
  
   $query = $pdo->prepare("
						   SELECT posts.*, users.* from `timeline`
						   INNER JOIN `posts` on timeline.postId = posts.postId
						   INNER JOIN `users` on posts.postUserId = users.uid
						   WHERE timeline.whoseTimeLine = :uid
						   ORDER By timeline.statusTime DESC
						   LIMIT $limit OFFSET $offset
						   "
					   );

   $query->bindParam(':uid', $uid, PDO::PARAM_STR);		 
   $query->execute();

   $errorData = $query->errorInfo();
   if($errorData[1]){
	   return checkError($response, $errorData);
   }
  
    $posts= $query->fetchAll(PDO::FETCH_OBJ);
    
	$output['status']  = 200;
	$output['message'] = "Newsfeed Loaded Successfully";
	$output['posts'] = $posts;


	$payload = json_encode($output);
	$response->getBody()->write($payload);
	return $response->withHeader('Content-Type', 'application/json')->withStatus(200);	
	  
		  
});

function checkRequest($userId,$profileId){
	include __DIR__ . '/../bootstrap/dbconnection.php';
	$stmt = $pdo->prepare("SELECT * FROM `requests` WHERE `sender` = :userId AND `receiver` = :profileId 
	OR `sender` = :profileId AND `receiver` = :userId");
	$stmt->bindParam(':userId', $userId, PDO::PARAM_STR);
	$stmt->bindParam(':profileId', $profileId, PDO::PARAM_STR);
	$stmt->execute();
	return $stmt->fetch(PDO::FETCH_ASSOC);
}

function checkFriend($userId,$profileId){
	include __DIR__ . '/../bootstrap/dbconnection.php';
	$stmt = $pdo->prepare("SELECT * FROM `friends` WHERE `userId` = :userId AND `profileId` = :profileId");
	$stmt->bindParam(':userId', $userId, PDO::PARAM_STR);
	$stmt->bindParam(':profileId', $profileId, PDO::PARAM_STR);
	$stmt->execute();
	return $stmt->fetch(PDO::FETCH_ASSOC);
}

?>




