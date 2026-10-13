"""Starter smoke test to verify the test runner is working."""

import unittest


class SmokeTest(unittest.TestCase):
    def test_workspace_is_ready_for_tests(self):
        self.assertTrue(True, "The automated test runner is working.")


if __name__ == "__main__":
    unittest.main()
