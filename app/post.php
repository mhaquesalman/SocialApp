<?php

//Api for inserting post
$app->post('/uploadpost', function ( $request,  $response,  $args) {

    /* 
        privacy level flag
        0  => friends
        1  => only me
        2  = > public

    */

include __DIR__ . '/../bootstrap/dbconnection.php';

$requestData = array();
$output = array();

$requestData['post'] = $request->getParsedBody()['post'];
$requestData['postUserId'] = $request->getParsedBody()['postUserId'];
$requestData['privacy'] = $request->getParsedBody()['privacy'];
$requestData['statusImage'] = "";

 if(isset($_FILES['file']['tmp_name'])){
        if (move_uploaded_file( $_FILES ['file'] ["tmp_name"], "../uploads/" . $_FILES ["file"] ["name"] )) {
            $requestData['statusImage'] = "../uploads/" . $_FILES ["file"] ["name"];
        }else{
            
            $output['status']  = 500;
            $output['message'] = "Couldn't Upload Image to Server !";

            $payload = json_encode($output);
            $response->getBody()->write($payload);

            return $response->withHeader('Content-Type', 'application/json')->withStatus(500);
        }
    }
 
 $query = $pdo->prepare("INSERT INTO `posts` 
    ( `post`, `postUserId`, `statusImage`, `statusTime`, `privacy`) VALUES 
    ( :post, :postUserId, :statusImage, current_timestamp, :privacy ); ");

$query->execute($requestData);
$errorData = $query->errorInfo();
if($errorData[1]){
    return checkError($response, $errorData);
}

$output['status']  = 200;
$output['message'] = "Post Uploaded Successfully !";

$payload = json_encode($output);
$response->getBody()->write($payload);
return $response->withHeader('Content-Type', 'application/json')->withStatus(200);

});


//Api for updating post
$app->post('/updatepost', function ( $request,  $response,  $args) {

    /* 
        privacy level flag
        0  => friends
        1  => only me
        2  = > public

    */

include __DIR__ . '/../bootstrap/dbconnection.php';

$output = array();

$postId = $request->getParsedBody()['postId'];
$post = $request->getParsedBody()['post'];
$privacy = $request->getParsedBody()['privacy'];
$statusImage = "";

 if(isset($_FILES['file']['tmp_name'])){
        if (move_uploaded_file( $_FILES ['file'] ["tmp_name"], "../uploads/" . $_FILES ["file"] ["name"] )) {
            $statusImage = "../uploads/" . $_FILES ["file"] ["name"];
        }else{
            
            $output['status']  = 500;
            $output['message'] = "Couldn't Upload Image to Server !";

            $payload = json_encode($output);
            $response->getBody()->write($payload);

            return $response->withHeader('Content-Type', 'application/json')->withStatus(500);
        }
    } else {
        $statusImage = $request->getParsedBody()['postImage'];
    }


$query = "UPDATE `posts` SET `post` = :post, `statusImage` = :postImage, 
`privacy` = :privacy, `statusTime` = current_timestamp() WHERE `postId` = :postId; ";

$query = $pdo->prepare($query);
$query->bindParam(':postId', $postId, PDO::PARAM_INT);
$query->bindParam(':post', $post, PDO::PARAM_STR);
$query->bindParam(':postImage', $statusImage, PDO::PARAM_STR);
$query->bindParam(':privacy', $privacy, PDO::PARAM_INT);
$query->execute();

$errorData = $query->errorInfo();
if($errorData[1]){
    return checkError($response, $errorData);
}

$output['status']  = 200;
$output['message'] = "Post Updated Successfully !";

$payload = json_encode($output);
$response->getBody()->write($payload);
return $response->withHeader('Content-Type', 'application/json')->withStatus(200);

});


//Api for deleting post
$app->get('/deletepost', function ( $request,  $response,  $args) {

    /* 
        privacy level flag
        0  => friends
        1  => only me
        2  => public

    */

include __DIR__ . '/../bootstrap/dbconnection.php';

$output = array();
$postId = $request->getQueryParams()['postId'];

/* $query = $pdo->prepare(" DELETE `posts.*`, `timeline.*` 
FROM `posts` INNER JOIN `timeline` 
WHERE posts.postId = timeline.postId AND posts.postId = :postId "); */

$query = $pdo->prepare(" DELETE `posts`, `timeline` 
FROM `posts` INNER JOIN `timeline`
ON posts.postId = timeline.postId WHERE posts.postId = :postId ");

$query->bindParam(':postId', $postId, PDO::PARAM_INT);
$query->execute();

$errorData = $query->errorInfo();
if($errorData[1]){
    return checkError($response, $errorData);
}

$query = $pdo->prepare("DELETE FROM `notifications` WHERE postId = :postId ");
$query->bindParam(':postId', $postId, PDO::PARAM_INT);
$query->execute();

$errorData = $query->errorInfo();
if($errorData[1]){
    return checkError($response, $errorData);
}

$output['status']  = 200;
$output['message'] = "Post Deleted Successfully !";

$payload = json_encode($output);
$response->getBody()->write($payload);
return $response->withHeader('Content-Type', 'application/json')->withStatus(200);

});

// fetching post details
$app->get('/postdetail',function($request, $response,  $args){
    include __DIR__ . '/../bootstrap/dbconnection.php';

    $postId = $request->getQueryParams()['postId'];
    $uid = $request->getQueryParams()['uid'];

     $stmt = $pdo->prepare("SELECT posts.*, users.name, users.profileUrl, users.userToken
                             FROM `posts` 
                             LEFT join users
                             ON posts.postUserId = users.uid
                             WHERE `postId` = :postId");
    $stmt->bindParam(':postId', $postId, PDO::PARAM_INT);		 
    $stmt->execute();
    $errorData = $stmt->errorInfo();
    if($errorData[1]){
        return checkError($response, $errorData);
    }
    $result[0] =$stmt->fetch(PDO::FETCH_OBJ);
    
    $reactionCheck = checkOurReact($uid, $result[0]->postId);
    if($reactionCheck){
     
     $result[0]->reactionType=$reactionCheck->reactionType;
    }else{
     $result[0]->reactionType="default";
    }
 
     $output['status']  = 200;
     $output['message'] = "Post Details Fetched";
     $output['posts'] = $result;

     $payload = json_encode($output,JSON_NUMERIC_CHECK);
     $response->getBody()->write($payload);
     return $response->withHeader('Content-Type', 'application/json')->withStatus(200);   
    
});

//Api for add/remove reaction
$app->post('/performreaction',function($request, $response,  $args){

    include __DIR__ .'/../bootstrap/dbconnection.php';

    $userId = $request->getParsedBody()['userId'];
    $postId =  $request->getParsedBody()['postId'];
    $postOwnerId =  $request->getParsedBody()['postOwnerId'];
    $previousReactionType = $request->getParsedBody()['previousReactionType'];
    $newReactionType = $request->getParsedBody()['newReactionType'];
    $message = "Operation Successful";

    /*
        (previousReactionType , newReactionType)
        like, love, care, haha, wow, sad, angry, default

        default -> used to undo reactions
    */

    $oldReactionColumn = checkColumnName($previousReactionType);
    $newReactionColumn = checkColumnName($newReactionType);

    if($newReactionType == "default"){
        
        if($previousReactionType == "default"){
            $message = "No Operation Performed";
            $reactions = getReactionCount($postId);
            $reactions->reactionType = 'default';
            
            $output['status']  = 200;
            $output['message'] = $message;
            $output['reaction'] = $reactions;

            $payload = json_encode($output,JSON_NUMERIC_CHECK);
            $response->getBody()->write($payload);
            return $response->withHeader('Content-Type', 'application/json')->withStatus(200);   
            
        }
            // decrease counter of old reaction
            $stmt = $pdo->prepare(" UPDATE `posts` 
                                SET ". $oldReactionColumn ." = " . $oldReactionColumn ." -1 "." 
                                WHERE `postId` = :postId");

            $stmt->bindParam(":postId", $postId, PDO::PARAM_INT);
            $stmt->execute();

            $errorData = $stmt->errorInfo();
            if($errorData[1]){
                return checkError($response, $errorData);
            }

        // remove old reaction from reaction table
        $stmt = $pdo->prepare( "DELETE FROM `reactions` WHERE
                                `reactionBy` = :userId AND
                                `postOn` = :postId 
                             ");

        $stmt->bindParam(":userId", $userId, PDO::PARAM_STR);
        $stmt->bindParam(":postId", $postId, PDO::PARAM_INT);
        $stmt->execute();

        $errorData = $stmt->errorInfo();
        if($errorData[1]){
        return checkError($response, $errorData);
        }  

        // removing notification sincne user undo the given reaction
        $nType = 'post-reaction';
        $stmt = $pdo->prepare("DELETE FROM  `notifications` WHERE 
        `notificationTo`=:notificationTo AND `notificationFrom` =:notificationFrom AND `postId` =:postId AND `type` =:type");
        $stmt->bindParam(':notificationTo', $postOwnerId, PDO::PARAM_STR);
        $stmt->bindParam(':notificationFrom', $userId, PDO::PARAM_STR);
        $stmt->bindParam(':postId', $postId, PDO::PARAM_INT);
        $stmt->bindParam(':type', $nType, PDO::PARAM_STR);
        $stmt= $stmt->execute();

        $message = "Reaction Undo Successfull";
    }else{
        // previous = care, newReaction = wow
        if($previousReactionType != "default"){
        // decrease counter of old reaction
        $stmt = $pdo->prepare(" UPDATE `posts` 
        SET ". $oldReactionColumn ." = " . $oldReactionColumn ." -1 "." 
        WHERE `postId` = :postId");

        $stmt->bindParam(":postId", $postId, PDO::PARAM_INT);
        $stmt->execute();

        $errorData = $stmt->errorInfo();
        if($errorData[1]){
        return checkError($response, $errorData);
        }

        // remove old reaction from reaction table
        $stmt = $pdo->prepare( "DELETE FROM `reactions` WHERE
        `reactionBy` = :userId AND
        `postOn` = :postId 
        ");

        $stmt->bindParam(":userId", $userId, PDO::PARAM_STR);
        $stmt->bindParam(":postId", $postId, PDO::PARAM_INT);
        $stmt->execute();

        $errorData = $stmt->errorInfo();
        if($errorData[1]){
        return checkError($response, $errorData);
        }
      }
       
        // increase counter of new reaction
        $stmt = $pdo->prepare(" UPDATE `posts` 
                            SET ". $newReactionColumn ." = " . $newReactionColumn ." +1 "." 
                            WHERE `postId` = :postId");

        $stmt->bindParam(":postId", $postId, PDO::PARAM_INT);
        $stmt->execute();

        $errorData = $stmt->errorInfo();
        if($errorData[1]){
            return checkError($response, $errorData);
        }

        // insert new reaction to reactions table
        $stmt = $pdo->prepare( "INSERT INTO `reactions` 
                                (`reactionBy`, `postOn`, `reactionType` ) 
                                VALUES (:reactionBy, :postOn, :reactionType); ");


        $stmt->bindParam(':reactionBy', $userId, PDO::PARAM_STR);
        $stmt->bindParam(':postOn', $postId, PDO::PARAM_INT);
        $stmt->bindParam(':reactionType', $newReactionType, PDO::PARAM_STR);

        $stmt->execute();

        $errorData = $stmt->errorInfo();
        if($errorData[1]){
            return checkError($response, $errorData);
        }

        if($userId != $postOwnerId){
            sendNotification($postOwnerId, $userId, $postId, 'post-reaction');
        }

        $message = "Reaction changed from ".$previousReactionType. " to ".$newReactionType;
    }
        // send back the updated reaction counts
        $reactions = getReactionCount($postId);
        $reactions->reactionType = $newReactionType;

        $output['status']  = 200;
        $output['message'] = $message;
        $output['reaction'] = $reactions;

        $payload = json_encode($output,JSON_NUMERIC_CHECK);
        $response->getBody()->write($payload);
        return $response->withHeader('Content-Type', 'application/json')->withStatus(200);         
    

});

function getReactionCount($postId){

    include __DIR__ . '/../bootstrap/dbconnection.php';

    $stmt =  $pdo->prepare("  SELECT 
                             `likeCount` , `loveCount`, `careCount`,
                             `hahaCount`, `wowCount`, `sadCount`, `angryCount`
                              from `posts` WHERE `postId` = :postId LIMIT 1");


    $stmt->bindParam(':postId', $postId, PDO::PARAM_INT);
    $stmt->execute();
    
    $errorData = $stmt->errorInfo();
    if($errorData[1]){
        return checkError($response, $errorData);
    }

    $userInfo = $stmt->fetch(PDO::FETCH_OBJ);
    return $userInfo;

    }

function checkColumnName ($reactionType){
    $columnName = "likeCount";
    if($reactionType == "love"){

        $columnName = "loveCount";

    }else if($reactionType == "care"){

       $columnName = "careCount";

    }else if($reactionType == "haha"){

       $columnName = "hahaCount";

    }else if($reactionType == "wow"){

       $columnName = "wowCount";

    }else if($reactionType == "sad"){

       $columnName = "sadCount";

    }else if($reactionType == "angry"){

       $columnName = "angryCount";

    }
    return $columnName;
}

?>