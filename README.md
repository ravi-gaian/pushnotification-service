# twitter-service

## Introduction
This service will upload/share content in twitter page


## Engagement Request Model
Below is the sample request model provided to publish content on twitter page through engagement API.

### To create text post
|# | value|
|:---------:|:-------------:|
|Method | POST|
|URI | http://{host}/engagements-web/v1.0/gaian/engagements||
|headers | Content-Type: application/json |
| -       |  Authorization: Bearer {token}|
|Body | {"channel": {"channelType": "TWITTER","configId": "12345c","providerId": "provider.used.to.create.config","config": {"title":"my twitter post"] } },"description": "testing twitter post","name": "twitter post test","source": {},"destination": {},"trigger": {"triggerType": "ONETIME"} }|

### To create post with media
|# | value|
|:---------:|:-------------:|
|Method | POST|
|URI | http://{host}/engagements-web/v1.0/gaian/engagements||
|headers | Content-Type: application/json |
| -       |  Authorization: Bearer {token}|
|Body | {"channel": {"channelType": "TWITTER","configId": "12345c","providerId": "provider.used.to.create.config","config": {"title":"my twitter post", "attachments":\[{"type":"IMAGE","mediaUrl":"http://my-media-url"},{"type":"VIDEO","mediaUrl":"http://my-media-url"},{"type":"LINK","mediaUrl":"http://my-link-url/to/attach"}] } },"description": "testing twitter post","name": "twitter post test","source": {},"destination": {},"trigger": {"triggerType": "ONETIME"} }|