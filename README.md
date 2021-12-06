# pushnotification-service

## Introduction
This service will upload/share content pushnotifications page


## PushNotification Request Model
Below is the sample request model provided to publish content on twitter page through engagement API.

curl --location --request POST 'http://localhost:8522/v1.0/11111111111111/pushnotification' \
--header 'Authorization: Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6ImZmOGYxNjhmLTNmZjYtNDZlMi1iMTJlLWE2YTdlN2Y2YTY5MCJ9.eyJwcm9maWxlVXJsIjpudWxsLCJzdWIiOiJnYWlhbi5jb20iLCJjb2xvciI6bnVsbCwidXNlcl9uYW1lIjoiZ3VycHJlZXRfZ2FuZGhpIiwiaXNzIjoiZ2FpYW4uY29tIiwidXNlck5hbWUiOiJndXJwcmVldF9nYW5kaGkiLCJhdXRob3JpdGllcyI6WyJST0xFX09NTklfQ09OU1VNRVIiLCJST0xFX01BUktFVFBMQUNFX1VTRVIiLCJST0xFX09NTklfVVNFUiJdLCJjbGllbnRfaWQiOiJnYWlhbiIsInNjb3BlIjpbInRydXN0IiwicmVhZCIsIndyaXRlIl0sInRlbmFudElkIjoiNjExZTA5MjhkNmQ1NTcwMDAxNTBhODdhIiwibG9nbyI6bnVsbCwiZXhwIjoxNjM4MzIyNDYxLCJqdGkiOiI1NDY5M2JlZi0zMjU2LTQzYTUtOWRmNC01ZmM4NDdjZDUyMzkiLCJlbWFpbCI6Imd1cnByZWV0X2dhbmRoaUBnYXRlc3RhdXRvbWF0aW9uLmNvbSJ9.YhnQiPqcwCJrq2LYxWtvc7gCRqXfO1f739mwUX1bBUI8CYMDutm1Kp8TGnLKSImixEgcmZJ45awkWPa7qS4xNwYEH-LTJMEwuMqQN-VNyYDVXulX_BdlSWV6PafeIASUgQ2AYxTCHwvPphkdgP_uNIzBVva9YuVSmgSr5xohyKyI89jmkFs1_KZTql3-23N5EfMRJ81uvQT9FuLcQKWY_34YU8qO77Khkx067UjmBLGanM9cXrMSd_uVWihSfFNlD4s3W5H5eEaozvEd9frYNekroNssSwJPlucyoHJJnVrf_HpnDmnzviO9V3FCfmp6m23E-yktWG7rugKngkcdOQ' \
--header 'Content-Type: application/json' \
--data-raw '{
    "transactionId": "1234",
    "engagement": {
        "tenantId": "11111111111111",
        "name": "push Test",
        "description": "push Test",
        "trigger": {
            "triggerType": "ONETIME"
        },
        "destination": {
            "type": "GROUP",
            "id": "5e4cd06e9464f859a0e2092e",
            "iteration": {
                "type": "PER_ROW"
            }
        },
        "channel": {
            "channelType": "PUSHNOTIFICATION",
            "config": {
                "title": "Hi is the revolution with API!"
            },
            "providerId": "com.gaian.voxa.ProviderImpl",
            "configId": "96ae8415-be10-4659-ab10-8719764ae580",
            "providers": {
                "api_url_fcm": "https://fcm.googleapis.com/fcm/send",
                "fcm_offset": "1",
                "auth_key_fcm": "AAAAJhi4XkM:APA91bFL2KR5i554NJukx7cs7dZgVHolalJRny16uezNQEvRPbJDRdcR0xS0OQo4_B4NjV15eQXYlzA35hh4L8BgQDLlLVNx1l-nEB8T-bmltoWYlUU7vnaWNEIBKV83FZSltl3qcsEe"
            },
            "payload": {
            "title": "Hi is the new revolution with API!",
            "body": "What a new revolution!",
            "id": "1",
            "url": "somethingshitty.com",
            "deviceIdsByType": {
                "ANDROID": [
                    "fxUpz_3Oo30:APA91bEGj57aSUvQ47OPgje0mcj9OKWxntID-qQlTNrGE4YI7-wWsQLxkhhAzg2btdHw53NYA7yHd4XXwkWSJash_L6zDkpp0ucSOxDyNRQ6rxq_ixlG3W4_VU8Ee5y2eNZ5NCfeyjCi"
                ]
            }
        }
        }
    }
}'
