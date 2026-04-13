import os
import json
import xml.etree.ElementTree as ET
from pymongo import MongoClient
from datetime import datetime
import glob
from pathlib import Path

def parse_surefire_reports():
    """Parse Surefire XML reports from target/surefire-reports/"""
    tests = []
    surefire_path = Path('target/surefire-reports')
    
    if not surefire_path.exists():
        print("⚠️ No surefire-reports folder found")
        return tests
    
    for xml_file in surefire_path.glob('*.xml'):
        try:
            tree = ET.parse(xml_file)
            root = tree.getroot()
            
            for testcase in root.findall('testcase'):
                test = {
                    'className': testcase.get('classname', ''),
                    'testName': testcase.get('name', ''),
                    'time': testcase.get('time', '0'),
                    'status': 'PASSED'
                }
                
                # Check for failure
                failure = testcase.find('failure')
                if failure is not None:
                    test['status'] = 'FAILED'
                    test['message'] = failure.get('message', '')
                    test['details'] = failure.text or ''
                
                # Check for error
                error = testcase.find('error')
                if error is not None:
                    test['status'] = 'ERROR'
                    test['message'] = error.get('message', '')
                    test['details'] = error.text or ''
                
                # Check for skipped
                skipped = testcase.find('skipped')
                if skipped is not None:
                    test['status'] = 'SKIPPED'
                
                tests.append(test)
                
        except Exception as e:
            print(f"❌ Error parsing {xml_file}: {e}")
    
    return tests

def parse_cucumber_reports():
    """Parse Cucumber JSON reports"""
    cucumber_data = None
    cucumber_json = Path('target/cucumber-reports/cucumber.json')
    
    if cucumber_json.exists():
        try:
            with open(cucumber_json, 'r', encoding='utf-8') as f:
                cucumber_data = json.load(f)
            print(f"✅ Parsed Cucumber JSON report")
        except Exception as e:
            print(f"❌ Error parsing Cucumber JSON: {e}")
    
    return cucumber_data

def get_test_summary(tests):
    """Calculate test summary statistics"""
    summary = {
        'total': len(tests),
        'passed': sum(1 for t in tests if t['status'] == 'PASSED'),
        'failed': sum(1 for t in tests if t['status'] == 'FAILED'),
        'error': sum(1 for t in tests if t['status'] == 'ERROR'),
        'skipped': sum(1 for t in tests if t['status'] == 'SKIPPED')
    }
    return summary

def list_report_files():
    """List all generated report files"""
    report_files = {
        'cucumber_reports': [],
        'surefire_reports': [],
        'test_output': []
    }
    
    # Cucumber reports
    cucumber_path = Path('target/cucumber-reports')
    if cucumber_path.exists():
        report_files['cucumber_reports'] = [str(f.relative_to('.')) for f in cucumber_path.rglob('*') if f.is_file()]
    
    # Surefire reports
    surefire_path = Path('target/surefire-reports')
    if surefire_path.exists():
        report_files['surefire_reports'] = [str(f.relative_to('.')) for f in surefire_path.rglob('*') if f.is_file()]
    
    # Test output
    test_output_path = Path('test-output')
    if test_output_path.exists():
        report_files['test_output'] = [str(f.relative_to('.')) for f in test_output_path.rglob('*') if f.is_file()]
    
    return report_files

def upload_to_mongodb():
    """Main function to upload test results to MongoDB"""
    print("=" * 60)
    print("📤 Uploading Test Results to MongoDB")
    print("=" * 60)
    
    # MongoDB connection
    mongo_uri = os.getenv('MONGO_URI', 'mongodb://admin:password123@localhost:27017')
    db_name = os.getenv('MONGO_DB', 'qa_test_results')
    collection_name = os.getenv('MONGO_COLLECTION', 'playwright_test_runs')
    
    try:
        # Connect to MongoDB
        print(f"🔗 Connecting to MongoDB...")
        client = MongoClient(mongo_uri, serverSelectionTimeoutMS=5000)
        # Test connection
        client.server_info()
        print(f"✅ Connected to MongoDB successfully")
        
        db = client[db_name]
        collection = db[collection_name]
        
        # Parse test results
        print("📊 Parsing test results...")
        surefire_tests = parse_surefire_reports()
        cucumber_data = parse_cucumber_reports()
        report_files = list_report_files()
        
        # Get test summary
        summary = get_test_summary(surefire_tests)
        
        # Build document
        test_run_document = {
            'source': 'QA_Playwright_Repo',
            'jobName': os.getenv('JOB_NAME', 'unknown'),
            'buildNumber': os.getenv('BUILD_NUMBER', 'local'),
            'buildUrl': os.getenv('BUILD_URL', ''),
            'timestamp': datetime.now().isoformat(),
            'executedBy': os.getenv('BUILD_USER', 'system'),
            
            'summary': summary,
            
            'surefireTests': surefire_tests,
            'cucumberTests': cucumber_data,
            
            'reportFiles': report_files,
            
            'environment': {
                'os': os.name,
                'workspace': os.getenv('WORKSPACE', os.getcwd())
            }
        }
        
        # Insert into MongoDB
        print("💾 Inserting document into MongoDB...")
        result = collection.insert_one(test_run_document)
        
        print("=" * 60)
        print("✅ SUCCESS! Test results uploaded to MongoDB")
        print("=" * 60)
        print(f"📝 Document ID: {result.inserted_id}")
        print(f"📊 Tests Summary:")
        print(f"   - Total: {summary['total']}")
        print(f"   - Passed: {summary['passed']}")
        print(f"   - Failed: {summary['failed']}")
        print(f"   - Error: {summary['error']}")
        print(f"   - Skipped: {summary['skipped']}")
        print(f"🏗️  Build: {test_run_document['buildNumber']}")
        print(f"📦 Job: {test_run_document['jobName']}")
        print("=" * 60)
        
        client.close()
        return 0
        
    except Exception as e:
        print("=" * 60)
        print(f"❌ ERROR: Failed to upload to MongoDB")
        print(f"❌ Error: {str(e)}")
        print("=" * 60)
        return 1

if __name__ == '__main__':
    exit(upload_to_mongodb())
