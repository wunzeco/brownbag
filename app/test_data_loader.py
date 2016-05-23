"""
  tests for data_loader module
"""

import data_loader

def test_webapp_index():
    """ index endpoint test """
    assert data_loader.index() == 'Shakespeare Play loader!'

def test_ping_es():
    """ cluster availability test """
    assert data_loader.ping_es() is True

def test_check_mapping_exists():
    """ verify mapping exists test """
    assert data_loader.check_mapping_exists() is True

def test_webapp_prepare():
    """ prepare elasticsearch for data loading test """
    assert data_loader.prepare() == 'elasticsearch readied!'

def test_webapp_load():
    """ load data test """
    assert data_loader.load() == 'Shakespeare Play data loaded!'
