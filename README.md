nlp-api-test

Sample application with REST api that calls on the [Google Cloud Natural Language API](https://cloud.google.com/natural-language/) and stores the results in ES for viewing in Kibana.

**Extra setup**
- You need Elasticsearch 2.3.1 installed and running.
-- Add VM option to application run for es.path.home: `-Des.path.home=<path>/elasticsearch-2.3.1` 
- You need a Google Cloud API Key (e.g. free trial)
-- Add VM option to application run for google.cloud.api.key `-Dgoogle.cloud.api.key=blablablabla`
- Probably some Kibana, since that is our goal ;)
