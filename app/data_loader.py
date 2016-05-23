"""
  This app loads data on Shakespeare Plays into elasticsearch.
  It is only a sample app for brownbag session at O2
"""

import logging
import json
import os
import sys
import bottle
import requests

logging.basicConfig(
    stream=sys.stdout,
    level=logging.DEBUG,
    format='%(levelname)s: %(message)s'
)

ES_CONN = '192.168.99.100:9200'
if os.getenv('ES_CONN'):
    ES_CONN = os.getenv('ES_CONN')

def ping_es():
    """ Check availability of ES cluster """
    es_url = "http://{0}/_cluster/health".format(ES_CONN)
    try:
        res = requests.get(es_url, timeout=2)
        res.raise_for_status()
    except requests.exceptions.RequestException, ex:
        logging.error("Cannot connect to elasticsearch on %s \n%s", es_url, ex)
        return False
    if json.loads(res.text) == 'red':
        return False

    return True


def check_mapping_exists():
    """ Check if index mapping exists in elasticsearch """
    es_url = "http://{0}/shakespeare".format(ES_CONN)
    if ping_es():
        res = requests.get(es_url)
        if res.status_code != 200:
            return False
    else:
        raise Exception('ConnectionError')

    return True


@bottle.route('/')
def index():
    """ returns index """
    return 'Shakespeare Play loader!'


@bottle.route('/prepare')
def prepare():
    """ creates index mapping for shakespeare play data """
    es_url = "http://{0}/shakespeare".format(ES_CONN)
    if not check_mapping_exists():
        res = requests.put(es_url, data=json.load(open('mapping_shakespeare.json')))
        if res.status_code == 200:
            logging.info("mapping successfully created")
    else:
        logging.debug("mapping exists")

    return "elasticsearch readied!"


@bottle.post('/load')
def load():
    """ loads shakespeare play data """
    es_url = "http://{0}/shakespeare/_bulk".format(ES_CONN)
    prepare()
    res = requests.post(es_url, data=open('shakespeare.json').read())
    res.raise_for_status()

    return 'Shakespeare Play data loaded!'


if __name__ == '__main__':
    bottle.run(host='0.0.0.0', port=8080)
