import csv
reader = csv.reader(open('photos_tags.csv'))

result = {}
for row in reader:
    key = row[0]
    if key in result:
        # implement your duplicate row handling here
        pass
    result[key] = row[1:]
print(result)
