# GH Banking API Assignment 
By Allan Davies

## Overview
First of all thank you for the opportunity to complete this assignment. I feel
my solution does the job, however it does have a few shortcomings.

My goal was to keep any asynchronous code only where it was required. I didn't 
want it to permeate through the whole code base. Dealing with synchronous code
provides for a more pleasant development experience. 

I used XTDB for persistence, formerly known as Crux. There are probably better
choices, however I have been using XTDB in my side projects, and I enjoyed 
exploring it with this assignment.

## Shortcomings
When using XTDB, you can submit a transaction, the db will then index it, some 
time later, the data will be available for querying. You can also wait for the
transaction to be indexed if you want to.

At present my code will not wait, it will submit then return an updated record 
to the user. In this way I can get updates back to the user faster, at the expense
of inconsistency. This will complicate the user interface, because it will need
to communicate failed transactions to the user at a later time. The API itself,
also ends up more complicated, because it needs to facilitate getting this extra
information to the user.

By adding a `:wait? true` argument to the `db.core/write-many` call, the request
will only return once the transaction has been indexed. This removes the need for
providing for inconsistent state, but slows down the API.

Lastly, my id generator code leaves a lot to be desired. I think an application 
crash could cause duplicated ids to be generated.

## Omissions
* I have not tested what will happen if the application crashes while XTDB is
  trying to catch up on it's indexing.
* I have tested the core functionality, but not added any integration tests
* I have not provided any facilities for tracking failed transactions. 
  (When XTDB's match operation fails)
  
## Load Testing Results
I made use of Jmeter for load testing. I have achieved the goal of 1000 concurrent
requests, however the application response time suffers.

The test I used can be found in this repo, `simple-test.jmx`. I only tested
creating an account and querying an account.

**Not wainting for transaction to be indexed**

    summary +  44546 in 00:00:15 = 3009.9/s Avg:   216 Min:     2 Max:   481 Err:     0 (0.00%) Active: 1000 Started: 1000 Finished: 0
    summary +  91926 in 00:00:30 = 3064.5/s Avg:   324 Min:   144 Max:   532 Err:     0 (0.00%) Active: 957 Started: 1000 Finished: 43
    summary = 136472 in 00:00:45 = 3046.3/s Avg:   289 Min:     2 Max:   532 Err:     0 (0.00%)
    summary +  63528 in 00:00:22 = 2846.6/s Avg:   277 Min:     1 Max:   529 Err:     0 (0.00%) Active: 0 Started: 1000 Finished: 1000
    summary = 200000 in 00:01:07 = 2979.9/s Avg:   285 Min:     1 Max:   532 Err:     0 (0.00%)
    Tidying up ...    @ Sun Oct 10 14:57:52 SAST 2021 (1633870672325)
    ... end of run
    
**Waiting for transaction to be indexed**

    summary +   4208 in 00:00:07 =  625.6/s Avg:   479 Min:   112 Max:  1053 Err:     0 (0.00%) Active: 671 Started: 671 Finished: 0
    summary +  35997 in 00:00:30 = 1199.7/s Avg:   813 Min:   319 Max:  1717 Err:     0 (0.00%) Active: 1000 Started: 1000 Finished: 0
    summary =  40205 in 00:00:37 = 1094.5/s Avg:   778 Min:   112 Max:  1717 Err:     0 (0.00%)
    summary +  34275 in 00:00:30 = 1142.7/s Avg:   875 Min:   327 Max:  1867 Err:     0 (0.00%) Active: 1000 Started: 1000 Finished: 0
    summary =  74480 in 00:01:07 = 1116.2/s Avg:   823 Min:   112 Max:  1867 Err:     0 (0.00%)
    summary +  23012 in 00:00:30 =  766.9/s Avg:  1289 Min:   469 Max:  1917 Err:     0 (0.00%) Active: 1000 Started: 1000 Finished: 0
    summary =  97492 in 00:01:37 = 1007.8/s Avg:   933 Min:   112 Max:  1917 Err:     0 (0.00%)
    summary +  22087 in 00:00:30 =  736.3/s Avg:  1358 Min:   556 Max:  2148 Err:     0 (0.00%) Active: 1000 Started: 1000 Finished: 0
    summary = 119579 in 00:02:07 =  943.6/s Avg:  1012 Min:   112 Max:  2148 Err:     0 (0.00%)
    summary +  27803 in 00:00:30 =  926.4/s Avg:  1086 Min:   476 Max:  2181 Err:     0 (0.00%) Active: 1000 Started: 1000 Finished: 0
    summary = 147382 in 00:02:37 =  940.3/s Avg:  1026 Min:   112 Max:  2181 Err:     0 (0.00%)
    summary +  28377 in 00:00:30 =  946.1/s Avg:  1055 Min:   440 Max:  1876 Err:     0 (0.00%) Active: 1000 Started: 1000 Finished: 0
    summary = 175759 in 00:03:07 =  941.2/s Avg:  1030 Min:   112 Max:  2181 Err:     0 (0.00%)
    summary +  24241 in 00:00:25 =  957.2/s Avg:   929 Min:     1 Max:  1922 Err:     0 (0.00%) Active: 0 Started: 1000 Finished: 1000
    summary = 200000 in 00:03:32 =  943.1/s Avg:  1018 Min:     1 Max:  2181 Err:     0 (0.00%)
    Tidying up ...    @ Sun Oct 10 15:42:55 SAST 2021 (1633873375333)
    ... end of run

## Running the application

To run: `clojure -M -m server.core`
To test: `clj -M:test`
To dev, jack in, then run `(start)`

## Conclusion

I could have done more, however I had to limit what I did to the time I had. 
Thank you again for the opportunity. I hope I get the chance to talk about this test
during an interview.
