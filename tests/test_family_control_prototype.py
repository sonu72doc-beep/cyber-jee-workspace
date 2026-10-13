"""Static regression tests for the family-control HTML prototype.

These tests validate the demo UI only. They do not claim that Android devices,
cloud sync, location, camera, SMS, or call-log integrations are live.
"""
from html.parser import HTMLParser
from pathlib import Path
import re
import unittest


PROTOTYPE = Path(__file__).resolve().parents[1] / "prototypes" / "family-control" / "index.html"


class DashboardParser(HTMLParser):
    def __init__(self):
        super().__init__()
        self.ids = set()
        self.buttons = []
        self.script_depth = 0
        self.script_text = []

    def handle_starttag(self, tag, attrs):
        attrs = dict(attrs)
        if attrs.get("id"):
            self.ids.add(attrs["id"])
        if tag == "button":
            self.buttons.append(attrs)
        if tag == "script":
            self.script_depth += 1

    def handle_endtag(self, tag):
        if tag == "script" and self.script_depth:
            self.script_depth -= 1

    def handle_data(self, data):
        if self.script_depth:
            self.script_text.append(data)


class FamilyControlPrototypeTests(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.html = PROTOTYPE.read_text(encoding="utf-8")
        cls.parser = DashboardParser()
        cls.parser.feed(cls.html)
        cls.js = "\n".join(cls.parser.script_text)

    def test_prototype_file_exists_and_has_viewport(self):
        self.assertTrue(PROTOTYPE.is_file())
        self.assertIn('name="viewport"', self.html)
        self.assertIn("Cyber Jee Family Control", self.html)

    def test_demo_is_explicitly_marked_as_simulated(self):
        self.assertIn("DEMO MODE", self.html)
        self.assertIn("All devices, location, reports, and requests below are simulated", self.html)
        self.assertIn("No real phone is connected", self.html)

    def test_pairing_form_has_expected_controls(self):
        for element_id in ("pair", "deviceName", "pairStatus", "deviceCount", "devices"):
            self.assertIn(element_id, self.parser.ids)
        self.assertIn("function addDevice()", self.js)
        self.assertIn("if(!name)", self.js)
        self.assertIn("if(count>=10)", self.js)
        self.assertIn("No phone has been paired", self.js)

    def test_approval_controls_are_simulation_only(self):
        self.assertIn("function decide(result)", self.js)
        self.assertIn("No camera action occurred", self.js)
        self.assertIn("Approve (simulate)", self.html)

    def test_sensitive_data_is_not_claimed_as_live(self):
        for phrase in (
            "No real location is displayed or collected",
            "No camera action occurred",
            "No real phone is connected",
        ):
            self.assertIn(phrase, self.html)

    def test_required_dashboard_sections_exist(self):
        for phrase in (
            "Managed devices",
            "Approval requests",
            "Live report preview",
            "Location sharing",
            "Privacy & activity audit",
        ):
            self.assertIn(phrase, self.html)

    def test_script_has_balanced_function_braces(self):
        # Lightweight sanity check; full browser interaction testing is still needed.
        self.assertRegex(self.js, r"function\s+addDevice\s*\(")
        self.assertRegex(self.js, r"function\s+decide\s*\(")
        self.assertEqual(self.js.count("{"), self.js.count("}"))


if __name__ == "__main__":
    unittest.main()
